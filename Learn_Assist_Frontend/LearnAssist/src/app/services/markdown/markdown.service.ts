import { Injectable } from '@angular/core';
import { Marked } from 'marked';
import DOMPurify from 'dompurify';

/**
 * Tailwind classes applied to the rendered elements, identical to the ones produced by the
 * previous hand-written renderer, so the chat looks the same.
 */
const ELEMENT_CLASSES: Record<string, string> = {
  H1: 'text-2xl font-bold mb-4 mt-6',
  H2: 'text-xl font-bold mb-3 mt-4',
  H3: 'text-lg font-semibold mb-2',
  P: 'mb-4',
  UL: 'space-y-1 mb-2',
  OL: 'space-y-1 mb-2',
  STRONG: 'font-semibold',
  EM: 'italic',
  A: 'text-indigo-600 hover:underline',
  PRE: 'bg-gray-100 p-3 rounded mb-4 overflow-x-auto text-sm',
};

const INLINE_CODE_CLASSES = 'bg-gray-100 text-sm font-mono px-1 py-0.5 rounded';

/** Only harmless formatting tags survive; no script, style, iframe, img, form, svg, etc. */
const ALLOWED_TAGS = [
  'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'p', 'br', 'hr', 'blockquote',
  'ul', 'ol', 'li', 'strong', 'em', 'b', 'i', 'del', 'a', 'code', 'pre',
  'table', 'thead', 'tbody', 'tr', 'th', 'td', 'div', 'span',
];
/** No event handlers (onerror, onclick...) and no style attribute. */
const ALLOWED_ATTR = ['href', 'title', 'class', 'target', 'rel'];
/** Links may only point to http(s) or mailto: no javascript:, data:, vbscript:, file:... */
const ALLOWED_URI_REGEXP = /^(?:https?:|mailto:)/i;

/**
 * Renders Markdown coming from users or from the LLM into HTML that is safe to bind with
 * [innerHTML]. The output is sanitized by DOMPurify here and sanitized again by Angular
 * (no bypassSecurityTrust* anywhere).
 */
@Injectable({ providedIn: 'root' })
export class MarkdownService {
  private readonly marked = new Marked({ gfm: true, breaks: true, async: false });
  private readonly purifier = DOMPurify(window);

  constructor() {
    this.purifier.addHook('afterSanitizeAttributes', (node: Element) => {
      const tag = node.tagName;
      if (tag === 'CODE') {
        node.setAttribute('class', node.parentElement?.tagName === 'PRE' ? 'code-block' : INLINE_CODE_CLASSES);
      } else if (tag === 'LI') {
        node.setAttribute('class', node.parentElement?.tagName === 'OL' ? 'list-decimal ml-6' : 'list-disc ml-6');
      } else if (ELEMENT_CLASSES[tag]) {
        node.setAttribute('class', ELEMENT_CLASSES[tag]);
      } else {
        node.removeAttribute('class');
      }
      if (tag === 'A') {
        node.setAttribute('target', '_blank');
        node.setAttribute('rel', 'noopener noreferrer');
      }
    });
  }

  render(content: string | null | undefined): string {
    if (!content) {
      return '';
    }
    const rawHtml = this.marked.parse(content) as string;
    const safeHtml = this.purifier.sanitize(rawHtml, {
      ALLOWED_TAGS,
      ALLOWED_ATTR,
      ALLOWED_URI_REGEXP,
      ALLOW_DATA_ATTR: false,
    });
    return `<div class="prose prose-sm max-w-full break-words">${safeHtml}</div>`;
  }
}
