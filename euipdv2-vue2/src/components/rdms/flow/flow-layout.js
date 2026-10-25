/**
 * Author: 周朔鹏
 * Date: 2026-10-07
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 *
 * 为缺少图形信息（BPMNDI）的流程定义自动排布坐标。
 * 平台默认定义多数只有流程语义，bpmn-js 导入后画布为空，查看和设计前先补齐。
 * 只排布 process 下的直接节点与顺序流，不处理泳道、子流程与边界事件。
 */

const BPMN = 'http://www.omg.org/spec/BPMN/20100524/MODEL';
const BPMNDI = 'http://www.omg.org/spec/BPMN/20100524/DI';
const DC = 'http://www.omg.org/spec/DD/20100524/DC';
const DI = 'http://www.omg.org/spec/DD/20100524/DI';
const COLOR = 'http://www.omg.org/spec/BPMN/non-normative/color/1.0';
const XMLNS = 'http://www.w3.org/2000/xmlns/';

const SIZE = {
  event: { w: 36, h: 36 },
  gateway: { w: 50, h: 50 },
  task: { w: 130, h: 64 },
  virtual: { w: 0, h: 12 }
};
const GAP_X = 70;
const GAP_Y = 36;
const MARGIN = 60;
const LANE_STEP = 14;

const NODE_PATTERN = /^(task|\w*Task|\w*Event|\w*Gateway|subProcess|callActivity)$/;

export function hasDiagram(xml) {
  if (!xml) {
    return false;
  }
  const doc = new DOMParser().parseFromString(xml, 'text/xml');
  return doc.getElementsByTagNameNS(BPMNDI, 'BPMNShape').length > 0;
}

/**
 * 已有图形信息时原样返回；否则按层次排布后写入 BPMNDiagram。
 * colors 可选，形如 { 节点标识: { fill, stroke } }。
 */
export function withLayout(xml, colors) {
  if (!xml) {
    return xml;
  }
  const doc = new DOMParser().parseFromString(xml, 'text/xml');
  if (doc.getElementsByTagName('parsererror').length) {
    return xml;
  }
  if (doc.getElementsByTagNameNS(BPMNDI, 'BPMNShape').length) {
    return xml;
  }
  const process = doc.getElementsByTagNameNS(BPMN, 'process')[0];
  if (!process) {
    return xml;
  }
  const nodes = [];
  const flows = [];
  for (let i = 0; i < process.childNodes.length; i++) {
    const el = process.childNodes[i];
    if (el.nodeType !== 1 || !el.getAttribute('id')) {
      continue;
    }
    if (el.localName === 'sequenceFlow') {
      flows.push({
        id: el.getAttribute('id'),
        source: el.getAttribute('sourceRef'),
        target: el.getAttribute('targetRef'),
        name: el.getAttribute('name') || ''
      });
    } else if (el.localName !== 'boundaryEvent' && NODE_PATTERN.test(el.localName)) {
      nodes.push({
        id: el.getAttribute('id'),
        kind: kindOf(el.localName),
        start: el.localName === 'startEvent',
        name: el.getAttribute('name') || ''
      });
    }
  }
  if (!nodes.length) {
    return xml;
  }
  const layout = computeLayout(nodes, flows);
  writeDiagram(doc, process, layout, colors || {});
  return new XMLSerializer().serializeToString(doc);
}

function kindOf(localName) {
  if (/Event$/.test(localName)) {
    return 'event';
  }
  if (/Gateway$/.test(localName)) {
    return 'gateway';
  }
  return 'task';
}

function computeLayout(nodes, flows) {
  const byId = {};
  nodes.forEach(function (node, index) {
    node.order = index;
    node.out = [];
    node.inn = [];
    byId[node.id] = node;
  });
  const edges = flows.filter(function (flow) {
    return byId[flow.source] && byId[flow.target] && flow.source !== flow.target;
  });
  edges.forEach(function (edge) {
    byId[edge.source].out.push(edge);
    byId[edge.target].inn.push(edge);
  });

  const roots = nodes.filter(function (node) { return node.start; })
    .concat(nodes.filter(function (node) { return !node.start && !node.inn.length; }));
  const state = {};
  const postorder = [];
  let discovery = 0;
  function visit(node) {
    state[node.id] = 1;
    node.rank = discovery++;
    node.out.forEach(function (edge) {
      const next = byId[edge.target];
      if (state[next.id] === 1) {
        edge.back = true;
      } else if (!state[next.id]) {
        visit(next);
      }
    });
    state[node.id] = 2;
    postorder.push(node);
  }
  roots.forEach(function (node) {
    if (!state[node.id]) {
      visit(node);
    }
  });
  nodes.forEach(function (node) {
    if (!state[node.id]) {
      visit(node);
    }
  });

  const topo = postorder.slice().reverse();
  topo.forEach(function (node) {
    node.layer = 0;
  });
  topo.forEach(function (node) {
    node.out.forEach(function (edge) {
      if (!edge.back) {
        const next = byId[edge.target];
        next.layer = Math.max(next.layer, node.layer + 1);
      }
    });
  });

  const all = nodes.slice();
  edges.forEach(function (edge) {
    const s = byId[edge.source];
    const t = byId[edge.target];
    edge.chain = [];
    if (edge.back || t.layer - s.layer < 2) {
      return;
    }
    let prev = segment(edge, s.id);
    s.out.splice(s.out.indexOf(edge), 1, prev);
    t.inn.splice(t.inn.indexOf(edge), 1);
    for (let l = s.layer + 1; l < t.layer; l++) {
      const v = { id: edge.id + '__' + l, kind: 'virtual', layer: l, rank: s.rank + (l - s.layer) / 100, out: [], inn: [prev] };
      prev.target = v.id;
      byId[v.id] = v;
      all.push(v);
      edge.chain.push(v);
      prev = segment(edge, v.id);
      v.out.push(prev);
    }
    prev.target = t.id;
    t.inn.push(prev);
  });

  const layers = [];
  all.slice().sort(function (a, b) { return a.rank - b.rank; }).forEach(function (node) {
    (layers[node.layer] = layers[node.layer] || []).push(node);
  });
  for (let i = 0; i < layers.length; i++) {
    layers[i] = layers[i] || [];
  }
  orderLayers(layers, byId);
  place(layers, byId);
  const routed = route(edges, layers, byId);
  normalize(all, routed);
  return { nodes: nodes, edges: routed };
}

function segment(edge, source) {
  return { id: edge.id, source: source, target: null };
}

function orderLayers(layers, byId) {
  function index() {
    layers.forEach(function (layer) {
      layer.forEach(function (node, i) {
        node.pos = i;
      });
    });
  }
  function sweep(from, to, step, pick) {
    for (let l = from; l !== to; l += step) {
      const layer = layers[l];
      layer.forEach(function (node) {
        const refs = pick(node).filter(function (edge) { return !edge.back; });
        node.bary = refs.length ? refs.reduce(function (sum, edge) {
          const other = byId[edge.source === node.id ? edge.target : edge.source];
          return sum + other.pos;
        }, 0) / refs.length : node.pos;
      });
      layer.sort(function (a, b) {
        return a.bary - b.bary || (a.kind === 'virtual') - (b.kind === 'virtual') || a.rank - b.rank;
      });
      layer.forEach(function (node, i) {
        node.pos = i;
      });
    }
  }
  index();
  for (let round = 0; round < 4; round++) {
    sweep(1, layers.length, 1, function (node) { return node.inn; });
    sweep(layers.length - 2, -1, -1, function (node) { return node.out; });
  }
  sweep(1, layers.length, 1, function (node) { return node.inn; });
  index();
}

function columnWidth(layer) {
  return layer.reduce(function (max, node) { return Math.max(max, SIZE[node.kind].w); }, 0);
}

function place(layers, byId) {
  let x = MARGIN;
  layers.forEach(function (layer) {
    const width = columnWidth(layer);
    layer.forEach(function (node) {
      node.w = SIZE[node.kind].w;
      node.h = SIZE[node.kind].h;
      node.cx = x + width / 2;
      node.colLeft = x;
    });
    x += width + GAP_X;
  });
  layers.forEach(function (layer, l) {
    let prev = null;
    layer.forEach(function (node) {
      const preds = node.inn.filter(function (edge) { return !edge.back && byId[edge.source].layer < l; });
      const floor = prev ? prev.cy + spacing(prev, node) : -Infinity;
      const desired = preds.length
        ? preds.reduce(function (sum, edge) { return sum + byId[edge.source].cy; }, 0) / preds.length
        : (prev ? floor : 0);
      node.cy = Math.max(desired, floor);
      prev = node;
    });
  });
}

function spacing(a, b) {
  const gap = a.kind === 'virtual' || b.kind === 'virtual' ? 16 : GAP_Y;
  return a.h / 2 + b.h / 2 + gap;
}

function bounds(node) {
  return { left: node.cx - node.w / 2, right: node.cx + node.w / 2, top: node.cy - node.h / 2, bottom: node.cy + node.h / 2 };
}

function blocked(node, column, fromY, toY) {
  const low = Math.min(fromY, toY);
  const high = Math.max(fromY, toY);
  return column.some(function (other) {
    if (other === node) {
      return false;
    }
    const b = bounds(other);
    return b.bottom > low && b.top < high;
  });
}

function route(edges, layers, byId) {
  let laneBelow = 0;
  let laneAbove = 0;
  const bends = {};
  const labelCtx = { labels: [], nodes: [] };
  layers.forEach(function (layer) {
    layer.forEach(function (node) {
      if (node.kind !== 'virtual') {
        labelCtx.nodes.push({ x: node.cx - node.w / 2, y: node.cy - node.h / 2, width: node.w, height: node.h });
        if (node.name && (node.kind === 'event' || node.kind === 'gateway')) {
          // 事件与网关的名称由 bpmn-js 放在图形正下方，连线标签也要让开
          const size = textSize(node.name);
          labelCtx.nodes.push({ x: node.cx - size.width / 2, y: node.cy + node.h / 2 + 4, width: size.width, height: size.height });
        }
      }
    });
  });
  function bendX(node, sourceId) {
    const used = bends[node.layer] = bends[node.layer] || {};
    if (used[sourceId] === undefined) {
      used[sourceId] = Object.keys(used).length;
    }
    return node.colLeft - GAP_X / 2 + ((used[sourceId] % 5) - 2) * 8;
  }
  return edges.map(function (edge) {
    const s = byId[edge.source];
    const t = byId[edge.target];
    const sb = bounds(s);
    const tb = bounds(t);
    let points;
    if (!edge.back && t.layer > s.layer) {
      if (!edge.chain.length && s.kind === 'gateway' && Math.abs(s.cy - t.cy) >= 1
          && !blocked(s, layers[s.layer], s.cy, t.cy)) {
        const exitY = t.cy > s.cy ? sb.bottom : sb.top;
        points = [[s.cx, exitY], [s.cx, t.cy], [tb.left, t.cy]];
      } else {
        const stops = edge.chain.map(function (v) { return { x: v.cx, y: v.cy, node: v }; })
          .concat([{ x: tb.left, y: t.cy, node: t }]);
        points = [[sb.right, s.cy]];
        stops.forEach(function (stop) {
          const last = points[points.length - 1];
          if (Math.abs(last[1] - stop.y) >= 1) {
            const midX = bendX(stop.node, s.id);
            points.push([midX, last[1]], [midX, stop.y]);
          }
          points.push([stop.x, stop.y]);
        });
        points = simplify(points);
      }
    } else {
      const span = layers.slice(Math.min(s.layer, t.layer), Math.max(s.layer, t.layer) + 1)
        .reduce(function (all, layer) { return all.concat(layer); }, []);
      const bottom = span.reduce(function (max, node) { return Math.max(max, bounds(node).bottom); }, -Infinity);
      const top = span.reduce(function (min, node) { return Math.min(min, bounds(node).top); }, Infinity);
      const belowFree = !blocked(s, layers[s.layer], s.cy, Infinity) && !blocked(t, layers[t.layer], t.cy, Infinity);
      const aboveFree = !blocked(s, layers[s.layer], -Infinity, s.cy) && !blocked(t, layers[t.layer], -Infinity, t.cy);
      if (belowFree) {
        const y = bottom + 24 + laneBelow++ * LANE_STEP;
        points = [[s.cx, sb.bottom], [s.cx, y], [t.cx, y], [t.cx, tb.bottom]];
      } else if (aboveFree) {
        const y = top - 24 - laneAbove++ * LANE_STEP;
        points = [[s.cx, sb.top], [s.cx, y], [t.cx, y], [t.cx, tb.top]];
      } else {
        const below = (bottom - s.cy) + (bottom - t.cy) <= (s.cy - top) + (t.cy - top);
        const y = below ? bottom + 24 + laneBelow++ * LANE_STEP : top - 24 - laneAbove++ * LANE_STEP;
        const outX = s.colLeft + columnWidth(layers[s.layer]) + GAP_X / 2 - LANE_STEP / 2;
        const inX = t.colLeft - GAP_X / 2 + LANE_STEP / 2;
        points = [[sb.right, s.cy], [outX, s.cy], [outX, y], [inX, y], [inX, t.cy], [tb.left, t.cy]];
      }
    }
    return { id: edge.id, points: points, label: labelBounds(edge.name, points, s, t, labelCtx) };
  });
}

/**
 * 连线名称的标签位置。放在进入终点前的最后一段上，而不是整条线的中点：
 * 同一来源扇出的多条线共用一段主干，中点标签会叠在一起，末段则各归各的终点。
 * 水平末段标签放线上方，竖直末段放线右侧。
 */
function labelBounds(name, points, s, t, ctx) {
  if (!name || points.length < 2) {
    return null;
  }
  // bpmn-js 会按实测文字尺寸围绕给定框的中心重新定位，估得越准，位置越稳。
  const size = textSize(name);
  const w = size.width;
  const h = size.height;
  const a = points[points.length - 2];
  const b = points[points.length - 1];
  const horizontal = Math.abs(a[1] - b[1]) < 1;
  const candidates = [];
  if (horizontal) {
    const dir = b[0] >= a[0] ? 1 : -1;
    const right = b[0] - dir * 6;
    const nearEnd = dir > 0 ? right - w : right;
    // 标签右端贴着终点，先试线上方，再试线下方
    candidates.push({ x: nearEnd, y: a[1] - h - 4, width: w, height: h });
    candidates.push({ x: nearEnd, y: a[1] + 4, width: w, height: h });
    if (points.length === 2) {
      // 相邻两节点之间放不下时，抬到两节点顶之上，居中于间隙
      const top = Math.min(bounds(s).top, bounds(t).top);
      candidates.push({ x: (a[0] + b[0]) / 2 - w / 2, y: top - h - 2, width: w, height: h });
    }
  } else {
    const dir = b[1] >= a[1] ? 1 : -1;
    const cy = b[1] - dir * (h / 2 + 10);
    candidates.push({ x: a[0] + 6, y: cy - h / 2, width: w, height: h });
    candidates.push({ x: a[0] - 6 - w, y: cy - h / 2, width: w, height: h });
  }
  // 再试整条线里的其他段：水平段放上方居中，竖直段放右侧或左侧居中
  segments(points).forEach(function (seg) {
    if (seg.horizontal && seg.length >= w + 12) {
      candidates.push({ x: seg.cx - w / 2, y: seg.cy - h - 4, width: w, height: h });
      candidates.push({ x: seg.cx - w / 2, y: seg.cy + 4, width: w, height: h });
    } else if (!seg.horizontal && seg.length >= h + 8) {
      candidates.push({ x: seg.cx + 6, y: seg.cy - h / 2, width: w, height: h });
      candidates.push({ x: seg.cx - 6 - w, y: seg.cy - h / 2, width: w, height: h });
    }
  });
  // 逐个候选位置先原位、再上下错开、最后左右挪半个宽度，取第一个不压节点也不压已放标签的位置
  const dys = [0, -1, 1, -2, 2, -3, 3];
  const dxs = [0, -(w / 2 + 4), w / 2 + 4];
  for (let k = 0; k < dxs.length; k++) {
    for (let j = 0; j < dys.length; j++) {
      for (let i = 0; i < candidates.length; i++) {
        const c = candidates[i];
        const box = { x: c.x + dxs[k], y: c.y + dys[j] * (h + 2), width: c.width, height: c.height };
        if (!collides(box, ctx.nodes) && !collides(box, ctx.labels)) {
          ctx.labels.push(box);
          return box;
        }
      }
    }
  }
  const fallback = candidates[0];
  ctx.labels.push(fallback);
  return fallback;
}

function segments(points) {
  const list = [];
  for (let i = 0; i + 1 < points.length; i++) {
    const p = points[i];
    const q = points[i + 1];
    const horizontal = Math.abs(p[1] - q[1]) < 1;
    list.push({
      horizontal: horizontal,
      length: horizontal ? Math.abs(q[0] - p[0]) : Math.abs(q[1] - p[1]),
      cx: (p[0] + q[0]) / 2,
      cy: (p[1] + q[1]) / 2
    });
  }
  return list.sort(function (m, n) { return n.length - m.length; });
}

function collides(box, rects) {
  const pad = 2;
  return rects.some(function (r) {
    return box.x < r.x + r.width + pad && box.x + box.width + pad > r.x
      && box.y < r.y + r.height + pad && box.y + box.height + pad > r.y;
  });
}

/** 外部标签宽度上限，与 bpmn-js 默认一致。 */
const LABEL_MAX_WIDTH = 90;

/** 按 bpmn-js 默认 11px 字号估算：中日韩字符约 11px，其余约 6px；超宽时按上限折行。 */
function textSize(text) {
  let w = 2;
  for (let i = 0; i < text.length; i++) {
    w += text.charCodeAt(i) > 0x2e7f ? 11 : 6;
  }
  w = Math.max(w, 20);
  if (w <= LABEL_MAX_WIDTH) {
    return { width: w, height: 16 };
  }
  const lines = Math.ceil(w / LABEL_MAX_WIDTH);
  return { width: LABEL_MAX_WIDTH, height: 13 * lines + 2 };
}

function simplify(points) {
  return points.filter(function (p, i) {
    if (i === 0 || i === points.length - 1) {
      return true;
    }
    const a = points[i - 1];
    const b = points[i + 1];
    return !((Math.abs(a[0] - p[0]) < 1 && Math.abs(p[0] - b[0]) < 1)
      || (Math.abs(a[1] - p[1]) < 1 && Math.abs(p[1] - b[1]) < 1));
  });
}

function normalize(nodes, edges) {
  let minY = Infinity;
  nodes.forEach(function (node) {
    minY = Math.min(minY, node.cy - node.h / 2);
  });
  edges.forEach(function (edge) {
    edge.points.forEach(function (p) {
      minY = Math.min(minY, p[1]);
    });
  });
  const shift = MARGIN - minY;
  nodes.forEach(function (node) {
    node.cy += shift;
  });
  edges.forEach(function (edge) {
    edge.points.forEach(function (p) {
      p[1] += shift;
    });
    if (edge.label) {
      edge.label.y += shift;
    }
  });
}

function writeDiagram(doc, process, layout, colors) {
  const defs = doc.documentElement;
  defs.setAttributeNS(XMLNS, 'xmlns:bpmndi', BPMNDI);
  defs.setAttributeNS(XMLNS, 'xmlns:dc', DC);
  defs.setAttributeNS(XMLNS, 'xmlns:di', DI);
  const colored = Object.keys(colors).length > 0;
  if (colored) {
    defs.setAttributeNS(XMLNS, 'xmlns:color', COLOR);
  }
  const old = doc.getElementsByTagNameNS(BPMNDI, 'BPMNDiagram');
  for (let i = old.length - 1; i >= 0; i--) {
    old[i].parentNode.removeChild(old[i]);
  }
  const diagram = doc.createElementNS(BPMNDI, 'bpmndi:BPMNDiagram');
  diagram.setAttribute('id', 'BPMNDiagram_auto');
  const plane = doc.createElementNS(BPMNDI, 'bpmndi:BPMNPlane');
  plane.setAttribute('id', 'BPMNPlane_auto');
  plane.setAttribute('bpmnElement', process.getAttribute('id'));
  layout.nodes.forEach(function (node) {
    const shape = doc.createElementNS(BPMNDI, 'bpmndi:BPMNShape');
    shape.setAttribute('id', node.id + '_di');
    shape.setAttribute('bpmnElement', node.id);
    if (node.kind === 'gateway') {
      shape.setAttribute('isMarkerVisible', 'true');
    }
    const color = colors[node.id];
    if (color) {
      if (color.fill) {
        shape.setAttributeNS(COLOR, 'color:background-color', color.fill);
      }
      if (color.stroke) {
        shape.setAttributeNS(COLOR, 'color:border-color', color.stroke);
      }
    }
    const box = doc.createElementNS(DC, 'dc:Bounds');
    box.setAttribute('x', String(Math.round(node.cx - node.w / 2)));
    box.setAttribute('y', String(Math.round(node.cy - node.h / 2)));
    box.setAttribute('width', String(node.w));
    box.setAttribute('height', String(node.h));
    shape.appendChild(box);
    plane.appendChild(shape);
  });
  layout.edges.forEach(function (edge) {
    const shape = doc.createElementNS(BPMNDI, 'bpmndi:BPMNEdge');
    shape.setAttribute('id', edge.id + '_di');
    shape.setAttribute('bpmnElement', edge.id);
    edge.points.forEach(function (p) {
      const point = doc.createElementNS(DI, 'di:waypoint');
      point.setAttribute('x', String(Math.round(p[0])));
      point.setAttribute('y', String(Math.round(p[1])));
      shape.appendChild(point);
    });
    if (edge.label) {
      const label = doc.createElementNS(BPMNDI, 'bpmndi:BPMNLabel');
      const box = doc.createElementNS(DC, 'dc:Bounds');
      box.setAttribute('x', String(Math.round(edge.label.x)));
      box.setAttribute('y', String(Math.round(edge.label.y)));
      box.setAttribute('width', String(Math.round(edge.label.width)));
      box.setAttribute('height', String(edge.label.height));
      label.appendChild(box);
      shape.appendChild(label);
    }
    plane.appendChild(shape);
  });
  diagram.appendChild(plane);
  defs.appendChild(diagram);
}
