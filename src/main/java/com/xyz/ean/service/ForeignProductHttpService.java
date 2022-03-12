package com.xyz.ean.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xyz.ean.dto.StandardProductDTO;
import com.xyz.ean.pojo.DomainUtils;
import com.xyz.ean.pojo.SessionInstance;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ForeignProductHttpService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private SessionInstance sessionInstance;

    @Autowired
    public ForeignProductHttpService(final RestTemplateBuilder restTemplateBuilder, final ObjectMapper objectMapper) {
        final Supplier<RestTemplate> restTemplateSupplier = () -> {
            final CloseableHttpClient httpClient =
                HttpClientBuilder.create()
                    .setRedirectStrategy(DefaultRedirectStrategy.INSTANCE)
                    .setDefaultCookieStore(new BasicCookieStore())
                    .build();

            return restTemplateBuilder
                .rootUri("https://apex.savegnago.com.br/apexmobile")
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
                .build();
        };

        this.restTemplate = restTemplateSupplier.get();
        this.objectMapper = objectMapper;
        this.createAnInstance();
    }

    private void createAnInstance() {
        final Document loginPageDocument = this.restTemplate.execute("/f?p=171", HttpMethod.GET, null,
            response -> Jsoup.parse(DomainUtils.readFromInputStream(response.getBody())));

        final String instanceId = Objects.requireNonNull(Objects.requireNonNull(loginPageDocument).getElementById("pInstance")).attr("value");

        final MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("p_flow_id", "171");
        requestBody.add("p_flow_step_id", "101");
        requestBody.add("p_request", "LOGIN");
        requestBody.addAll("p_arg_names", List.of("1984683107054336023", "1984683185872336024"));
        requestBody.add("p_t01", System.getenv("SAVEG_LOGIN"));
        requestBody.add("p_t02", System.getenv("SAVEG_PASSWORD"));
        requestBody.add("p_md5_checksum", "");
        requestBody.add("p_instance", instanceId);
        requestBody.add("p_page_submission_id", Objects.requireNonNull(loginPageDocument.getElementById("pPageSubmissionId")).attr("value"));
        requestBody.add("p_page_checksum", Objects.requireNonNull(loginPageDocument.getElementById("pPageChecksum")).attr("value"));

        this.restTemplate.postForEntity("/wwv_flow.accept", requestBody, String.class);

        final String ajaxIdentifier = this.restTemplate.execute("/f?p=171:2:"+instanceId+":NEXT:NO:2:P2_CURSOR:B", HttpMethod.GET, null,
            response -> {
                final String html = DomainUtils.readFromInputStream(response.getBody());

                final Matcher mtc = Pattern.compile("\"ajaxIdentifier\":\"([A-Z0-9]+?)\"").matcher(html);
                if (!mtc.find()) throw new IllegalStateException("Ajax identifier not found");
                return mtc.group(1);
            }
        );

        this.sessionInstance = new SessionInstance(instanceId, ajaxIdentifier);

    }

    public Optional<StandardProductDTO> fetchByEanCode(final String eanCode) {
        final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

        final MultiValueMap<String, String> body = new LinkedMultiValueMap<>(6);
        body.add("p_request", sessionInstance.getAjaxIdentifier());
        body.add("p_flow_id", "171");
        body.add("p_flow_step_id", "2");
        body.add("p_instance", sessionInstance.getSessionId());
        body.add("p_debug", "");
        body.addAll("p_arg_names", List.of("P2_CURSOR", "P2_LOJA_ID", "P2_COD1"));
        body.addAll("p_arg_values", List.of("B", "221", eanCode));

        final HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, headers);

        return restTemplate.execute(
            "/wwv_flow.show",
            HttpMethod.POST,
            restTemplate.httpEntityCallback(httpEntity, String.class),
            (clientHttpResponse) -> {
                final String json = DomainUtils.readFromInputStream(clientHttpResponse.getBody());
                final StandardProductDTO standardProductDTO = new StandardProductDTO();
                final JsonNode jsonNode = objectMapper.readTree(json).get("item");

                if (Objects.isNull(jsonNode)) {
                    this.createAnInstance();
                    return fetchByEanCode(eanCode);
                }

                if (jsonNode.get(5).get("value").asText().length() == 0)
                    return Optional.empty();

                final String description = jsonNode.get(1).get("value").asText();
                final int sequence = jsonNode.get(2).get("value").asInt();
                final double currentPriceValue = DomainUtils.parsePrice(jsonNode.get(4).get("value").asText());
                final String eanCodeValue = jsonNode.get(5).get("value").asText();

                standardProductDTO.setDescription(description);
                standardProductDTO.setSequence(sequence);
                standardProductDTO.setCurrentPrice(currentPriceValue);
                standardProductDTO.setEanCode(eanCodeValue);

                return Optional.of(standardProductDTO);
            }
        );
    }
}
