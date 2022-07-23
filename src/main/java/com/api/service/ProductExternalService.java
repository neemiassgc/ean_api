package com.api.service;

import com.api.entity.SessionStorage;
import com.api.pojo.DomainUtils;
import com.api.pojo.SessionInstance;
import com.api.projection.Projection;
import com.api.repository.SessionStorageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.util.Pair;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.api.projection.Projection.ProductBase;

@Service
@Log4j2
public class ProductExternalService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SessionStorageRepository sessionStorageRepository;

    @Setter(AccessLevel.PRIVATE)
    private SessionInstance sessionInstance;

    @Autowired
    public ProductExternalService(
        final RestTemplateBuilder restTemplateBuilder,
        final ObjectMapper objectMapper,
        final SessionStorageRepository sessionStorageRepository
    ) {
        this.objectMapper = objectMapper;
        this.sessionStorageRepository = sessionStorageRepository;

        final CookieStore basicCookieStore = new BasicCookieStore();

        final SessionStorage actualSession =
            sessionStorageRepository.findTopByOrderByCreationDateDesc().orElseThrow();

        if (actualSession.getCreationDate().isEqual(LocalDate.now())) {
            log.info("Reusing session from DB");
            this.sessionInstance = new SessionInstance(actualSession.getInstance() + "", actualSession.getAjaxId());

            final BasicClientCookie cookie = new BasicClientCookie(actualSession.getCookieKey(), actualSession.getCookieValue());
            cookie.setPath("/");
            cookie.setSecure(true);
            cookie.setDomain("apex.savegnago.com.br");
            cookie.setExpiryDate(Date.from(Instant.now().plus(10, ChronoUnit.DAYS)));
            basicCookieStore.addCookie(cookie);
        }
        else this.sessionInstance = SessionInstance.EMPTY_SESSION;

        final int twoSeconds = (int) Duration.ofSeconds(2).toMillis();
        final RequestConfig requestConfig = RequestConfig
            .custom()
            .setConnectionRequestTimeout(twoSeconds)
            .setConnectTimeout(twoSeconds)
            .setConnectTimeout(twoSeconds)
            .build();

        final CloseableHttpClient httpClient = HttpClientBuilder.create()
            .setDefaultRequestConfig(requestConfig)
            .setRedirectStrategy(DefaultRedirectStrategy.INSTANCE)
            .setDefaultCookieStore(basicCookieStore)
            .build();

        this.restTemplate = restTemplateBuilder
            .rootUri("https://apex.savegnago.com.br/apexmobile")
            .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
            .build();
    }

    private Map<String, String> initialScrapingRequest() {
        final Map<String, String> resourcesToReturn = new HashMap<>(3);

        // Checking for resources from the login page....
        log.info("Scraping login page to get form fields");
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

    private Pair<String, String> loginRequest(final Map<String, String> resourcesMap) {
        assert resourcesMap != null;
        assert resourcesMap.containsKey("instance_id");
        assert resourcesMap.containsKey("submission_id");
        assert resourcesMap.containsKey("checksum");

        // Login with the credentials..
        log.info("Creating a request body for a POST request using "+MediaType.APPLICATION_FORM_URLENCODED_VALUE+" as media type");
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
        log.info("Logging into the system to get a Cookie session and a AjaxId");
        final ResponseEntity<?> responseEntity = this.restTemplate.postForEntity("/wwv_flow.accept", requestBody, String.class);
        final String cookie = Objects.requireNonNull(responseEntity.getHeaders().get("Set-Cookie")).get(0);

        // Request to get the 'ajaxIdentifier'
        final String ajaxId = this.restTemplate.execute(
            "/f?p=171:2:"+resourcesMap.get("instance_id")+":NEXT:NO:2:P2_CURSOR:B",
            HttpMethod.GET,
            null,
            response -> {
                final String html = DomainUtils.readFromInputStream(response.getBody());

                final Matcher mtc = Pattern.compile("\"ajaxIdentifier\":\"([A-Z0-9]+?)\"").matcher(html);
                if (!mtc.find()) throw new IllegalStateException("Ajax identifier not found");
                return mtc.group(1);
            }
        );

        return Pair.of(Objects.requireNonNull(ajaxId), cookie);
    }

    public SessionInstance newSessionInstance() {
        final Map<String, String> resourcesMap = this.initialScrapingRequest();
        final Pair<String, String> pairOfResources = this.loginRequest(resourcesMap);

        final String[] pairOfCookies = pairOfResources.getSecond().split("=");
        final SessionStorage sessionStorage = new SessionStorage();
        sessionStorage.setInstance(Long.parseLong(resourcesMap.get("instance_id"), 10));
        sessionStorage.setAjaxId(pairOfResources.getFirst());
        sessionStorage.setCookieKey(pairOfCookies[0]);
        sessionStorage.setCookieValue(pairOfCookies[1]);
        sessionStorage.setCreationDate(LocalDate.now());

        log.info("Saving a new session in the DB");
        sessionStorageRepository.save(sessionStorage);

        return new SessionInstance(sessionStorage.getInstance()+"", pairOfResources.getFirst());
    }

    public Optional<ProductBase> fetchByBarcode(final String barcode) {
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

        log.info("Fetching for information about a product by barcode");
        return restTemplate.execute(
            "/wwv_flow.show",
            HttpMethod.POST,
            restTemplate.httpEntityCallback(httpEntity, String.class),
            (clientHttpResponse) -> {
                final String json = DomainUtils.readFromInputStream(clientHttpResponse.getBody());

                try {
                    final ProductBase productWithLatestPrice =
                        objectMapper.readValue(json, Projection.ProductWithLatestPrice.class);

                    return Optional.of(productWithLatestPrice);
                }
                catch (InvalidDefinitionException | IllegalStateException exception) {
                    if (exception instanceof IllegalStateException)
                        if (exception.getMessage().equals("Item name is empty"))
                            return Optional.empty();

                    this.setSessionInstance(this.newSessionInstance());
                    return fetchByBarcode(barcode);
                }
            }
        );
    }
}
