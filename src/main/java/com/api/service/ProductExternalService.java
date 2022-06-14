package com.api.service;

import com.api.projection.InputItemDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.api.pojo.DomainUtils;
import com.api.pojo.SessionInstance;
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

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProductExternalService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private SessionInstance sessionInstance;

    @Autowired
    public ProductExternalService(final RestTemplateBuilder restTemplateBuilder, final ObjectMapper objectMapper) {
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
        this.sessionInstance = SessionInstance.EMPTY_SESSION;
    }

    private Map<String, String> initialScrapingRequest() {
        final Map<String, String> resourcesToReturn = new HashMap<>(3);

        final Document loginPageDocument = DomainUtils.requireNonNull(
            this.restTemplate.execute(
                "/f?p=171",
                HttpMethod.GET,
                null,
                response -> Jsoup.parse(DomainUtils.readFromInputStream(response.getBody()))
            ),
            new IllegalStateException("Login page parsing failed")
        );

        final String instanceId = DomainUtils.requireNonNull(
            loginPageDocument.getElementById("pInstance"),
            new IllegalStateException("Element with id 'pInstance' not found")
        ).attr("value");

        final String submissionId =  DomainUtils.requireNonNull(
                loginPageDocument.getElementById("pPageSubmissionId"),
                new IllegalStateException("Element with id 'pPageSubmissionId' not found")
        ).attr("value");

        final String checkSum = DomainUtils.requireNonNull(
            loginPageDocument.getElementById("pPageChecksum"),
            new IllegalStateException("Element with id 'pPageChecksum' not found")
        ).attr("value");

        resourcesToReturn.put("instance_id", instanceId);
        resourcesToReturn.put("submission_id", submissionId);
        resourcesToReturn.put("checksum", checkSum);

        return resourcesToReturn;
    }

    private String loginRequest(final Map<String, String> resourcesMap) {
        assert resourcesMap != null;
        assert resourcesMap.containsKey("instance_id");
        assert resourcesMap.containsKey("submission_id");
        assert resourcesMap.containsKey("checksum");

        final MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("p_flow_id", "171");
        requestBody.add("p_flow_step_id", "101");
        requestBody.add("p_request", "LOGIN");
        requestBody.addAll("p_arg_names", List.of("1984683107054336023", "1984683185872336024"));
        requestBody.add("p_t01", System.getenv("SAVEG_LOGIN"));
        requestBody.add("p_t02", System.getenv("SAVEG_PASSWORD"));
        requestBody.add("p_md5_checksum", "");
        requestBody.add("p_instance", resourcesMap.get("instance_id"));
        requestBody.add("p_page_submission_id", resourcesMap.get("submission_id"));
        requestBody.add("p_page_checksum", resourcesMap.get("checksum"));

        // It's just to get a cookie for the session
        this.restTemplate.postForEntity("/wwv_flow.accept", requestBody, String.class);

        return this.restTemplate.execute("/f?p=171:2:"+resourcesMap.get("instance_id")+":NEXT:NO:2:P2_CURSOR:B", HttpMethod.GET, null,
            response -> {
                final String html = DomainUtils.readFromInputStream(response.getBody());

                final Matcher mtc = Pattern.compile("\"ajaxIdentifier\":\"([A-Z0-9]+?)\"").matcher(html);
                if (!mtc.find()) throw new IllegalStateException("Ajax identifier not found");
                return mtc.group(1);
            }
        );
    }

    public SessionInstance getASessionInstance() {
        final Map<String, String> resourcesMap = this.initialScrapingRequest();
        final String ajaxIdentifier = this.loginRequest(resourcesMap);

        return new SessionInstance(resourcesMap.get("instance_id"), ajaxIdentifier);
    }

    public Optional<InputItemDTO> fetchByEanCode(final String barcode) {
        Objects.requireNonNull(barcode);

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
        body.addAll("p_arg_values", List.of("B", "221", barcode));

        final HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, headers);

        return restTemplate.execute(
            "/wwv_flow.show",
            HttpMethod.POST,
            restTemplate.httpEntityCallback(httpEntity, String.class),
            (clientHttpResponse) -> {
                final String json = DomainUtils.readFromInputStream(clientHttpResponse.getBody());

                try {
                    final InputItemDTO inputItemDTO = objectMapper.readValue(json, InputItemDTO.class);

                    if (Objects.isNull(inputItemDTO)) {
                        this.setSessionInstance(this.getASessionInstance());
                        return fetchByEanCode(barcode);
                    }

                    return Optional.of(inputItemDTO);
                } catch (Exception e)  {
                    return Optional.empty();
                }
            }
        );
    }

    private void setSessionInstance(SessionInstance sessionInstance) {
        this.sessionInstance = sessionInstance;
    }
}
