/**
 * Author: 周朔鹏
 * Date: 2026-10-06
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 *
 * 设计器侧栏与 BPMN 扩展元素 euipd:node 的读写。
 * bpmn-js 不认识该命名空间，导入前先读出，保存前再写回。
 */

const BPMN = 'http://www.omg.org/spec/BPMN/20100524/MODEL';
const EUIPD = 'http://euipd.com/bpmn';

export function emptyConfig() {
  return {
    resolver: '',
    fallback: '',
    denyCode: '',
    roleCode: '',
    userId: '',
    formCode: '',
    guardStates: '',
    guardDenyCode: '',
    timerDuration: '',
    timerAction: '',
    timerRepeat: false,
    onEnter: [],
    onComplete: []
  };
}

export function blankDiagram(processKey, name) {
  const key = processKey || 'process';
  const title = name || key;
  return '<?xml version="1.0" encoding="UTF-8"?>'
    + '<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"'
    + ' xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"'
    + ' xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"'
    + ' xmlns:euipd="http://euipd.com/bpmn"'
    + ' id="Definitions_1" targetNamespace="http://euipd.com/flow">'
    + '<bpmn:process id="' + key + '" name="' + title + '" isExecutable="true">'
    + '<bpmn:startEvent id="start" name="开始"/>'
    + '</bpmn:process>'
    + '<bpmndi:BPMNDiagram id="BPMNDiagram_1">'
    + '<bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="' + key + '">'
    + '<bpmndi:BPMNShape id="start_di" bpmnElement="start">'
    + '<dc:Bounds x="180" y="160" width="36" height="36"/>'
    + '</bpmndi:BPMNShape>'
    + '</bpmndi:BPMNPlane>'
    + '</bpmndi:BPMNDiagram>'
    + '</bpmn:definitions>';
}

export function readNodeConfigs(xml) {
  const configs = {};
  if (!xml) {
    return configs;
  }
  const doc = new DOMParser().parseFromString(xml, 'text/xml');
  const tasks = doc.getElementsByTagNameNS(BPMN, 'userTask');
  for (let i = 0; i < tasks.length; i++) {
    const task = tasks[i];
    const id = task.getAttribute('id');
    const node = firstDescendant(task, 'node');
    if (!id || !node) {
      continue;
    }
    configs[id] = configFromNode(node);
  }
  return configs;
}

export function applyNodeConfigs(xml, processKey, processName, configs) {
  const doc = new DOMParser().parseFromString(xml, 'text/xml');
  const defs = doc.getElementsByTagNameNS(BPMN, 'definitions')[0] || doc.documentElement;
  defs.setAttributeNS('http://www.w3.org/2000/xmlns/', 'xmlns:euipd', EUIPD);
  const process = doc.getElementsByTagNameNS(BPMN, 'process')[0];
  if (process) {
    if (processKey) {
      process.setAttribute('id', processKey);
      const plane = doc.getElementsByTagNameNS('http://www.omg.org/spec/BPMN/20100524/DI', 'BPMNPlane')[0];
      if (plane) {
        plane.setAttribute('bpmnElement', processKey);
      }
    }
    if (processName) {
      process.setAttribute('name', processName);
    }
  }
  const tasks = doc.getElementsByTagNameNS(BPMN, 'userTask');
  for (let i = 0; i < tasks.length; i++) {
    writeTask(doc, tasks[i], configs[tasks[i].getAttribute('id')]);
  }
  return new XMLSerializer().serializeToString(doc);
}

function configFromNode(node) {
  const cfg = emptyConfig();
  const assignee = firstChild(node, 'assignee');
  if (assignee) {
    cfg.resolver = assignee.getAttribute('resolver') || '';
    cfg.fallback = assignee.getAttribute('fallback') || '';
    cfg.denyCode = assignee.getAttribute('denyCode') || '';
    cfg.roleCode = assignee.getAttribute('roleCode') || '';
    cfg.userId = assignee.getAttribute('userId') || '';
  }
  const form = firstChild(node, 'form');
  if (form) {
    cfg.formCode = form.getAttribute('code') || '';
  }
  const guard = firstChild(node, 'guard');
  if (guard) {
    cfg.guardStates = guard.getAttribute('sourceStates') || '';
    cfg.guardDenyCode = guard.getAttribute('denyCode') || '';
  }
  const timer = firstChild(node, 'timer');
  if (timer) {
    cfg.timerDuration = timer.getAttribute('duration') || '';
    cfg.timerAction = timer.getAttribute('action') || '';
    cfg.timerRepeat = timer.getAttribute('repeat') === 'true';
  }
  cfg.onEnter = readActions(node, 'onEnter');
  cfg.onComplete = readActions(node, 'onComplete');
  return cfg;
}

function readActions(node, name) {
  return children(node, name).map(function (el) {
    return {
      action: el.getAttribute('action') || '',
      when: el.getAttribute('when') || '',
      params: paramsText(el)
    };
  });
}

function paramsText(el) {
  const parts = [];
  for (let i = 0; i < el.attributes.length; i++) {
    const attr = el.attributes[i];
    if (attr.name === 'action' || attr.name === 'when' || attr.name.indexOf('xmlns') === 0) {
      continue;
    }
    parts.push(attr.name + '=' + attr.value);
  }
  return parts.join(', ');
}

function writeTask(doc, task, cfg) {
  const existing = children(task, 'extensionElements');
  existing.forEach(function (el) {
    task.removeChild(el);
  });
  if (!hasContent(cfg)) {
    return;
  }
  const ext = doc.createElementNS(BPMN, 'bpmn:extensionElements');
  const node = doc.createElementNS(EUIPD, 'euipd:node');
  if (cfg.resolver || cfg.fallback || cfg.denyCode || cfg.roleCode || cfg.userId) {
    const assignee = doc.createElementNS(EUIPD, 'euipd:assignee');
    setAttr(assignee, 'resolver', cfg.resolver);
    setAttr(assignee, 'fallback', cfg.fallback);
    setAttr(assignee, 'denyCode', cfg.denyCode);
    setAttr(assignee, 'roleCode', cfg.roleCode);
    setAttr(assignee, 'userId', cfg.userId);
    node.appendChild(assignee);
  }
  if (cfg.formCode) {
    const form = doc.createElementNS(EUIPD, 'euipd:form');
    form.setAttribute('code', cfg.formCode);
    node.appendChild(form);
  }
  if (cfg.guardStates || cfg.guardDenyCode) {
    const guard = doc.createElementNS(EUIPD, 'euipd:guard');
    setAttr(guard, 'sourceStates', cfg.guardStates);
    setAttr(guard, 'denyCode', cfg.guardDenyCode);
    node.appendChild(guard);
  }
  if (cfg.timerDuration || cfg.timerAction) {
    const timer = doc.createElementNS(EUIPD, 'euipd:timer');
    setAttr(timer, 'duration', cfg.timerDuration);
    setAttr(timer, 'action', cfg.timerAction);
    if (cfg.timerRepeat) {
      timer.setAttribute('repeat', 'true');
    }
    node.appendChild(timer);
  }
  appendActions(doc, node, 'euipd:onEnter', cfg.onEnter);
  appendActions(doc, node, 'euipd:onComplete', cfg.onComplete);
  ext.appendChild(node);
  const first = task.firstChild;
  if (first) {
    task.insertBefore(ext, first);
  } else {
    task.appendChild(ext);
  }
}

function appendActions(doc, node, tag, rows) {
  (rows || []).forEach(function (row) {
    if (!row || !row.action) {
      return;
    }
    const el = doc.createElementNS(EUIPD, tag);
    el.setAttribute('action', row.action);
    setAttr(el, 'when', row.when);
    parseParams(row.params).forEach(function (pair) {
      el.setAttribute(pair.name, pair.value);
    });
    node.appendChild(el);
  });
}

function parseParams(text) {
  if (!text) {
    return [];
  }
  return text.split(',').map(function (part) {
    return part.trim();
  }).filter(function (part) {
    return part && part.indexOf('=') > 0;
  }).map(function (part) {
    const index = part.indexOf('=');
    return { name: part.substring(0, index).trim(), value: part.substring(index + 1).trim() };
  });
}

function hasContent(cfg) {
  if (!cfg) {
    return false;
  }
  if (cfg.resolver || cfg.fallback || cfg.denyCode || cfg.roleCode || cfg.userId
    || cfg.formCode || cfg.guardStates || cfg.guardDenyCode || cfg.timerDuration || cfg.timerAction) {
    return true;
  }
  return (cfg.onEnter || []).some(function (row) { return row && row.action; })
    || (cfg.onComplete || []).some(function (row) { return row && row.action; });
}

function setAttr(el, name, value) {
  if (value) {
    el.setAttribute(name, value);
  }
}

function children(el, local) {
  const found = [];
  if (!el) {
    return found;
  }
  for (let i = 0; i < el.childNodes.length; i++) {
    const child = el.childNodes[i];
    if (child.nodeType === 1 && child.localName === local) {
      found.push(child);
    }
  }
  return found;
}

function firstChild(el, local) {
  const list = children(el, local);
  return list.length ? list[0] : null;
}

function firstDescendant(el, local) {
  const list = [];
  collect(el, local, list);
  return list.length ? list[0] : null;
}

function collect(el, local, acc) {
  for (let i = 0; i < el.childNodes.length; i++) {
    const child = el.childNodes[i];
    if (child.nodeType !== 1) {
      continue;
    }
    if (child.localName === local) {
      acc.push(child);
    }
    collect(child, local, acc);
  }
}
