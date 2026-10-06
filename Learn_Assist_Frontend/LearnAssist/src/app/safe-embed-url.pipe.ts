import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

/**
 * External video providers that may be embedded in an <iframe>: host -> required path prefix.
 * Keep in sync with the backend VideoUrlPolicy.
 */
const ALLOWED_EMBEDS: Record<string, string> = {
  'www.youtube.com': '/embed/',
  'youtube.com': '/embed/',
  'www.youtube-nocookie.com': '/embed/',
  'player.vimeo.com': '/video/',
};

/**
 * Returns the normalized URL if it is an HTTPS embed URL of an allow-listed provider,
 * null otherwise (javascript:, data:, file:, http:, unknown hosts, credentials, ports...).
 */
export function toAllowedEmbedUrl(value: string | null | undefined): string | null {
  if (!value || value.length > 500) {
    return null;
  }
  let url: URL;
  try {
    url = new URL(value.trim());
  } catch {
    return null;
  }
  const requiredPrefix = ALLOWED_EMBEDS[url.hostname.toLowerCase()];
  const isAllowed =
    url.protocol === 'https:' &&
    !url.username &&
    !url.password &&
    (url.port === '' || url.port === '443') &&
    !!requiredPrefix &&
    url.pathname.startsWith(requiredPrefix) &&
    url.pathname.length > requiredPrefix.length;
  return isAllowed ? url.href : null;
}

/**
 * Turns an external video URL into a resource URL for an <iframe>, ONLY if it passes the
 * allow-list above. Angular requires a SafeResourceUrl for iframe[src], so the trust call
 * is applied exclusively to a re-serialized URL that was validated first; anything else
 * yields null and no iframe is rendered.
 */
@Pipe({
  name: 'safeEmbedUrl',
  standalone: true,
})
export class SafeEmbedUrlPipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {}

  transform(url: string | null | undefined): SafeResourceUrl | null {
    const allowed = toAllowedEmbedUrl(url);
    return allowed ? this.sanitizer.bypassSecurityTrustResourceUrl(allowed) : null;
  }
}
