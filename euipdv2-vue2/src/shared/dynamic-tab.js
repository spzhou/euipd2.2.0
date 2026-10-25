/**
 * 工作区动态页签的打开与关闭。
 * 关闭仍是先删掉当前项，再激活前一个下标；没有前一个时回到 fallback。
 */

export function tabId(prefix, index) {
  return prefix + '-' + index;
}

export function openOrFocusTab(vm, list, item, prefix, options) {
  const opts = options || {};
  const idx = list.findIndex(function (row) {
    return row && item && row.id === item.id;
  });
  if (idx < 0) {
    if (opts.markMain && typeof vm.isMainProjectRow === 'function') {
      item.isMainProject = vm.isMainProjectRow(item);
    }
    list.push(item);
    vm.activePageId = tabId(prefix, list.length - 1);
  } else {
    if (opts.refresh) {
      vm.$set(list, idx, item);
    }
    vm.activePageId = tabId(prefix, idx);
  }
}

export function closeDynamicTab(vm, spec) {
  const list = spec.list;
  let index = spec.index;
  list.splice(index, 1);
  index = index - 1;
  if (index >= 0) {
    vm.activePageId = tabId(spec.prefix, index);
  } else if (typeof spec.fallback === 'function') {
    vm.activePageId = spec.fallback.call(vm);
  } else {
    vm.activePageId = spec.fallback;
  }
  if (typeof spec.afterClose === 'function') {
    spec.afterClose.call(vm);
  }
}
