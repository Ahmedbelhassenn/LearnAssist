import { TestBed } from '@angular/core/testing';
import { MarkdownService } from './markdown.service';

/** Regression tests for audit finding F-02 (XSS in the chatbot Markdown renderer). */
describe('MarkdownService', () => {
  let service: MarkdownService;

  /** Parses the rendered HTML in a detached document so nothing executes. */
  function parse(html: string): Document {
    return new DOMParser().parseFromString(html, 'text/html');
  }

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MarkdownService);
  });

  it('renders the usual Markdown formatting', () => {
    const doc = parse(service.render('# Title\n\n**bold** and *italic* and `code`\n\n* item'));

    expect(doc.querySelector('h1')?.textContent).toBe('Title');
    expect(doc.querySelector('strong')?.textContent).toBe('bold');
    expect(doc.querySelector('em')?.textContent).toBe('italic');
    expect(doc.querySelector('code')?.textContent).toBe('code');
    expect(doc.querySelector('li')?.textContent).toBe('item');
  });

  it('keeps the previous Tailwind classes', () => {
    const doc = parse(service.render('## Sub\n\n[link](https://example.com)'));

    expect(doc.querySelector('h2')?.className).toBe('text-xl font-bold mb-3 mt-4');
    expect(doc.querySelector('a')?.className).toBe('text-indigo-600 hover:underline');
  });

  it('removes <script> tags', () => {
    const html = service.render('hello <script>alert(1)</script>');

    expect(parse(html).querySelector('script')).toBeNull();
    expect(html).not.toContain('alert(1)');
  });

  it('removes event handler attributes such as onerror and onclick', () => {
    const html = service.render('<img src=x onerror="alert(1)"> <a href="https://a.b" onclick="alert(2)">x</a> <div onmouseover="alert(3)">y</div>');
    const doc = parse(html);

    expect(doc.querySelector('img')).toBeNull();
    doc.querySelectorAll('*').forEach(el => {
      Array.from(el.attributes).forEach(attr => expect(attr.name.startsWith('on')).toBeFalse());
    });
  });

  it('drops javascript:, data: and vbscript: links', () => {
    const doc = parse(service.render(
      '[a](javascript:alert(1)) [b](JaVaScRiPt:alert(1)) [c](data:text/html,<script>alert(1)</script>) <a href="vbscript:msgbox(1)">d</a>'));

    doc.querySelectorAll('a').forEach(a => {
      const href = a.getAttribute('href');
      expect(href === null || /^(https?:|mailto:)/i.test(href)).toBeTrue();
    });
  });

  it('opens safe links in a new tab without opener access', () => {
    const a = parse(service.render('[ok](https://example.com)')).querySelector('a');

    expect(a?.getAttribute('href')).toBe('https://example.com');
    expect(a?.getAttribute('target')).toBe('_blank');
    expect(a?.getAttribute('rel')).toBe('noopener noreferrer');
  });

  it('removes iframes, styles, forms and svg', () => {
    const doc = parse(service.render(
      '<iframe src="https://evil.com"></iframe><style>body{display:none}</style><form action="https://evil.com"><input></form><svg onload="alert(1)"></svg>'));

    ['iframe', 'style', 'form', 'input', 'svg'].forEach(tag => expect(doc.querySelector(tag)).toBeNull());
  });

  it('does not let user-supplied classes overlay the page', () => {
    const div = parse(service.render('<div class="fixed inset-0 z-50">x</div>')).querySelector('.prose div');

    expect(div?.getAttribute('class')).toBeNull();
  });

  it('returns an empty string for empty content', () => {
    expect(service.render('')).toBe('');
    expect(service.render(null)).toBe('');
  });
});
