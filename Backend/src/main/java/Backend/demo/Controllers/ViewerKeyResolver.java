package Backend.demo.Controllers;

import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

public final class ViewerKeyResolver {

    private ViewerKeyResolver() {
    }

    public static String resolve(String rawViewerKey) {
        if (!StringUtils.hasText(rawViewerKey)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing X-Viewer-Key header");
        }

        String viewerKey = rawViewerKey.trim();
        if (viewerKey.length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Viewer-Key too long");
        }

        return viewerKey;
    }
}
