package com.api.components;

import com.api.entity.SessionStorage;
import com.api.pojo.Constants;
import com.api.pojo.DomainUtils;
import com.api.pojo.SessionInstance;
import com.api.service.interfaces.SessionStorageService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Log4j2
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProductSessionInstance {

    private final RestTemplate restTemplate;
    private final CookieStore cookieStore;
    private final SessionStorageService sessionStorageService;

    @Setter(AccessLevel.PRIVATE)
    @Getter
    private SessionInstance sessionInstance;

    @PostConstruct
    void init() {
        final Optional<SessionStorage> currentSession = findSessionStorage();
        final LocalDate todaySDate = LocalDate.now(ZoneId.of(Constants.TIMEZONE));

        currentSession
            .filter(session -> session.getCreationDate().equals(todaySDate))
            .ifPresentOrElse(this::useExistingSession, this::useEmptySession);
    }

    private Optional<SessionStorage> findSessionStorage() {
        final Sort.Order orderByCreationDateDesc = Sort.Order.desc("creationDate");
        final Sort.Order orderByIdAsc = Sort.Order.asc("id");
        return sessionStorageService.findTopBy(Sort.by(orderByCreationDateDesc, orderByIdAsc));
    }

    private void addCookieSessionToCookiesContext(final BasicClientCookie cookie) {
        final Date tenDaysInTheFuture = Date.from(Instant.now().plus(10, ChronoUnit.DAYS));
        final String targetDomain = "apex.savegnago.com.br";

        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setDomain(targetDomain);
        cookie.setExpiryDate(tenDaysInTheFuture);

        cookieStore.addCookie(cookie);
    }

    private void useExistingSession(final SessionStorage session) {
        log.info("Using an existing session");
        setSessionInstance(new SessionInstance(session.getInstance() + "", session.getAjaxId()));

        log.info("Using a cookie session");
        final BasicClientCookie cookie = new BasicClientCookie(session.getCookieKey(), session.getCookieValue());
        addCookieSessionToCookiesContext(cookie);
    }

    private void useEmptySession() {
        log.info("Using an empty session");
        setSessionInstance(SessionInstance.EMPTY_SESSION);
    }

    private SessionStorage buildSessionStorageAndSave() throws IOException {
        log.info("Building a SessionStorage instance to save after");
        final Map<String, String> formFields = crawlFormFields();
        final String instanceId = formFields.get("instance_id");
        final Cookie cookie = buildSessionCookie(formFields);
        final String ajaxIdentifier = requestAjaxIdentifier(instanceId);

        final SessionStorage sessionStorage = new SessionStorage();
        final LocalDate todaySDate = LocalDate.now(ZoneId.of(Constants.TIMEZONE));

        sessionStorage.setInstance(Long.parseLong(instanceId, 10));
        sessionStorage.setAjaxId(ajaxIdentifier);
        sessionStorage.setCookieKey(cookie.getName());
        sessionStorage.setCookieValue(cookie.getValue());
        sessionStorage.setCreationDate(todaySDate);

        log.info("Saving a new session in the DB");
        sessionStorageService.save(sessionStorage);

        return sessionStorage;
    }

    private SessionInstance newSessionInstance() throws IOException {
        log.info("Creating a new instance session");
        cookieStore.clear();

        final SessionStorage sessionStorage = buildSessionStorageAndSave();
        return new SessionInstance(sessionStorage.getInstance()+"", sessionStorage.getAjaxId());
    }

    private Document crawlLoginPage() throws IOException {
        log.info("Crawling login page to get form fields");

        final HttpEntity<String> response = restTemplate.getForEntity("/f?p=171", String.class);
        final String html = DomainUtils.requireNonNull(response.getBody(), new IllegalStateException("Login page parsing failed"));
        return Jsoup.parse(html);
    }

    private ResponseEntity<String> loginIntoTheSystem(final MultiValueMap<String, String> bodyWithCredentials) {
        log.info("Logging into the system");
        return restTemplate.postForEntity("/wwv_flow.accept", bodyWithCredentials, String.class);
    }

    private String requestAjaxIdentifier(final String instanceId) {
        log.info("Requesting Ajax-Identifier page");

        final String url = "/f?p=171:2:"+instanceId+":NEXT:NO:2:P2_CURSOR:B";
        final HttpEntity<String> targetPageToExtract = restTemplate.getForEntity(url, String.class);
        return extractAjaxIdentifier(targetPageToExtract);
    }

    private String extractAjaxIdentifier(final HttpEntity<String> page) {
        log.info("Extracting Ajax-Identifier");

        final String html = DomainUtils.requireNonNull(
            page.getBody(),
            new IllegalStateException("Extracting Ajax-Identifier page failed")
        );

        final Matcher mtc = Pattern.compile("\"ajaxIdentifier\":\"([A-Z0-9]+?)\"").matcher(html);
        if (!mtc.find()) throw new IllegalStateException("Ajax identifier not found");
        return mtc.group(1);
    }

    private Map<String, String> crawlFormFields() throws IOException {
        log.info("Crawl form fields");

        final Map<String, String> formFields = new HashMap<>(3);
        final Document crawledLoginPage = crawlLoginPage();

        final Element instanceId = crawledLoginPage.getElementById("pInstance");
        final Element submissionId = crawledLoginPage.getElementById("pPageSubmissionId");
        final Element checksum = crawledLoginPage.getElementById("pPageChecksum");

        formFields.put(
            "instance_id",
            DomainUtils.requireNonNull(
                instanceId,
                new IllegalStateException("Element with id 'pInstance' not found")
            )
            .attr("value")
        );
        formFields.put(
            "submission_id",
            DomainUtils.requireNonNull(
                submissionId,
                new IllegalStateException("Element with id 'pPageSubmissionId' not found")
            )
            .attr("value")
        );
        formFields.put(
            "checksum",
            DomainUtils.requireNonNull(
                checksum,
                new IllegalStateException("Element with id 'pPageChecksum' not found")
            )
            .attr("value")
        );

        return formFields;
    }

    private MultiValueMap<String, String> buildRequestBodyWithCredentials(final Map<String, String> formFields) {
        log.info("Creating a request body to login");

        assert formFields.containsKey("instance_id");
        assert formFields.containsKey("submission_id");
        assert formFields.containsKey("checksum");

        final MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>(10);

        requestBody.add("p_flow_id", "171");
        requestBody.add("p_flow_step_id", "101");
        requestBody.add("p_request", "LOGIN");
        requestBody.addAll("p_arg_names", List.of("1984683107054336023", "1984683185872336024"));
        requestBody.add("p_t01", System.getenv("SAVEG_LOGIN"));
        requestBody.add("p_t02", System.getenv("SAVEG_PASSWORD"));
        requestBody.add("p_md5_checksum", "");
        requestBody.add("p_instance", formFields.get("instance_id"));
        requestBody.add("p_page_submission_id", formFields.get("submission_id"));
        requestBody.add("p_page_checksum", formFields.get("checksum"));

        return requestBody;
    }

    private Cookie extractSessionCookie(final ResponseEntity<String> responseEntity) {
        log.info("Extracting a session cookie");
        final List<String> header = DomainUtils.requireNonNull(
            responseEntity.getHeaders().get("Set-Cookie"),
            new IllegalStateException("Cookie not found")
        );

        final String[] slicedCookie = header.get(0).split("=");

        assert slicedCookie.length == 2;

        final String cookieKey = slicedCookie[0];
        final String cookieValue = slicedCookie[1];

        return new BasicClientCookie(cookieKey, cookieValue);
    }

    private Cookie buildSessionCookie(final Map<String, String> formFields) {
        log.info("Building a session cookie");
        final MultiValueMap<String, String> bodyWithCredentials = buildRequestBodyWithCredentials(formFields);
        final ResponseEntity<String> responseEntity = loginIntoTheSystem(bodyWithCredentials);
        return extractSessionCookie(responseEntity);
    }

    public void reloadSessionInstance() throws IOException {
        log.info("Reloading session instance");
        setSessionInstance(newSessionInstance());
    }
}
