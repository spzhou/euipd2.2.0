/**
 * Author: 周朔鹏
 * Date: 2026-10-07
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 *
 * 把 bpmn-js 导出的 SVG 作为文件下载。查看器着色靠页面样式，导出时把着色
 * 规则内联进 SVG，使下载的图保留已完成与当前节点的颜色。
 */

const INLINE_STYLE = [
  '.flow-finished:not(.djs-connection) .djs-visual > :nth-child(1) { stroke: #67c23a !important; fill: #f0f9eb !important; }',
  '.flow-current:not(.djs-connection) .djs-visual > :nth-child(1) { stroke: #409eff !important; stroke-width: 3px !important; fill: #ecf5ff !important; }',
  '.djs-connection.flow-finished .djs-visual > path { stroke: #67c23a !important; }'
].join('\n');

export function downloadSvg(svg, fileName) {
  if (!svg) {
    return;
  }
  const styled = svg.indexOf('<defs>') > 0
    ? svg.replace('<defs>', '<defs><style>' + INLINE_STYLE + '</style>')
    : svg.replace(/(<svg[^>]*>)/, '$1<style>' + INLINE_STYLE + '</style>');
  const blob = new Blob([styled], { type: 'image/svg+xml;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName || 'flow.svg';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  setTimeout(function () {
    URL.revokeObjectURL(url);
  }, 1000);
}
