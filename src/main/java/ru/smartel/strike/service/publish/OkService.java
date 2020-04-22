package ru.smartel.strike.service.publish;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;
import ru.smartel.strike.configuration.properties.OkProperties;
import ru.smartel.strike.entity.Video;
import ru.smartel.strike.entity.interfaces.PostEntity;

import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
public class OkService {
    private static final Logger logger = LoggerFactory.getLogger(OkService.class);
    public static final String SEND_MESSAGE_URL = "https://api.ok.ru/fb.do?application_key={appKey}" +
            "&attachment={attachment}" +
            "&format=json" +
            "&gid={gid}" +
            "&method=mediatopic.post" +
            "&type=GROUP_THEME" +
            "&sig={sig}" +
            "&access_token={accessToken}";
    private final RestTemplate restTemplate;
    private final OkProperties properties;

    public OkService(RestTemplate restTemplate, OkProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    /**
     * Send post content and links to OK group
     *
     * @param post post to publish
     */
    @Async
    public void sendToGroup(PostEntity post) {
        if (!properties.isSpecified()) {
            return;
        }
        logger.info("Sending post {} with id {} to OK", post.getClass().getSimpleName(), post.getId());

        String attachment = "{\"type\":\"text\",\"text\":\"" + post.getContentRu() + "\"}";
        if (!post.getVideos().isEmpty()) {
            attachment = attachment + post.getVideos().stream()
                    .map(Video::getUrl)
                    .map(url -> "{\"type\":\"link\",\"url\":\"" + url + "\"}")
                    .collect(Collectors.joining(", ", ",", ""));
        }
        if (nonNull(post.getSourceLink())) {
            attachment = attachment + "," + "{\"type\":\"link\",\"url\":\"" + post.getSourceLink() + "\"}";
        }

        attachment = "{\"media\":[" + attachment + "]}";
        attachment = attachment.replace("\r", " ");

        String signature = getSig(properties.getAppKey(), attachment, properties.getGid(),
                properties.getAccessToken(), properties.getAppSecret());

        try {
            System.out.println(restTemplate.getForObject(
                    new UriTemplate(SEND_MESSAGE_URL).expand(
                            properties.getAppKey(), attachment, properties.getGid(), signature, properties.getAccessToken()),
                    String.class));
        } catch (RestClientException ex) {
            logger.error("Sending to OK group failed: {}", ex.getMessage());
        }
    }

    /**
     * Generate signature. See OK API doc for details
     */
    private String getSig(String applicationKey, String attachment, String gid, String accessToken, String appSecret) {
        return DigestUtils.md5Hex("application_key=" + applicationKey +
                "attachment=" + attachment +
                "format=json" +
                "gid=" + gid +
                "method=mediatopic.post" +
                "type=GROUP_THEME" +
                DigestUtils.md5Hex(accessToken + appSecret));
    }
}