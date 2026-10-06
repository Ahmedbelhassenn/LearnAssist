package com.example.LearnAssist.Storage;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

/** A stored file ready to be served, with the media type derived from its extension. */
public record StoredFile(Resource resource, MediaType mediaType, String fileName) {
}
