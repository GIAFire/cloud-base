<template>
  <div class="chat-markdown" v-dompurify-html="renderedMarkdown" @click="handleContentClick"></div>
</template>

<script setup lang="ts">
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.min.css'

defineOptions({ name: 'AgentChatMarkdown' })

const props = defineProps<{
  content: string
}>()

const message = useMessage()
const md = new MarkdownIt({
  html: false,
  breaks: true,
  linkify: true,
  highlight(code, language) {
    const escapedLanguage = md.utils.escapeHtml(language || 'text')
    let highlighted = md.utils.escapeHtml(code)
    if (language && hljs.getLanguage(language)) {
      try {
        highlighted = hljs.highlight(code, { language, ignoreIllegals: true }).value
      } catch {}
    }
    return `<pre class="chat-code"><button type="button" class="chat-code-copy" data-code-copy>复制</button><code class="hljs language-${escapedLanguage}">${highlighted}</code></pre>`
  }
})

const defaultLinkOpen = md.renderer.rules.link_open
md.renderer.rules.link_open = (tokens, index, options, env, self) => {
  tokens[index].attrSet('target', '_blank')
  tokens[index].attrSet('rel', 'noopener noreferrer')
  return defaultLinkOpen
    ? defaultLinkOpen(tokens, index, options, env, self)
    : self.renderToken(tokens, index, options)
}

const renderedMarkdown = computed(() => md.render(props.content || ''))

const handleContentClick = async (event: MouseEvent) => {
  const target = event.target as HTMLElement | null
  const button = target?.closest<HTMLElement>('[data-code-copy]')
  if (!button) return
  const code = button.parentElement?.querySelector('code')?.textContent || ''
  try {
    await navigator.clipboard.writeText(code)
    message.success('代码已复制')
  } catch {
    message.error('复制失败，请手动复制')
  }
}
</script>

<style lang="scss">
.chat-markdown {
  min-width: 0;
  overflow-wrap: anywhere;
  font-size: 14px;
  line-height: 1.7;

  > :first-child {
    margin-top: 0;
  }

  > :last-child {
    margin-bottom: 0;
  }

  p,
  ul,
  ol,
  blockquote,
  pre,
  table {
    margin: 0 0 12px;
  }

  ul,
  ol {
    padding-left: 22px;
  }

  blockquote {
    padding-left: 12px;
    color: var(--el-text-color-secondary);
    border-left: 3px solid var(--el-border-color);
  }

  :not(pre) > code {
    padding: 2px 5px;
    color: var(--el-color-danger);
    background: var(--el-fill-color-light);
    border-radius: 4px;
  }

  .chat-code {
    position: relative;
    overflow: auto;
    border-radius: 8px;
  }

  .chat-code-copy {
    position: absolute;
    top: 8px;
    right: 8px;
    z-index: 1;
    padding: 3px 8px;
    color: #d1d5db;
    cursor: pointer;
    background: rgb(17 24 39 / 78%);
    border: 1px solid rgb(255 255 255 / 18%);
    border-radius: 4px;
  }

  pre code.hljs {
    display: block;
    min-width: max-content;
    padding: 36px 16px 16px;
  }

  table {
    display: block;
    max-width: 100%;
    overflow-x: auto;
    border-collapse: collapse;
  }

  th,
  td {
    padding: 6px 10px;
    border: 1px solid var(--el-border-color);
  }

  a {
    color: var(--el-color-primary);
  }
}
</style>
