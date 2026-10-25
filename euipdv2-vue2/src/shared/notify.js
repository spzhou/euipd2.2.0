/**
 * 一次动作发布一个域事件。页面用 subscribe 接收；尚未改写的列表仍用 $on 登记，
 * publish 按域直接调用这些登记，不再额外 $emit 同名事件。
 */
import { invalidateAreas } from './http';

export const TOPIC = 'workspace.changed';

export const AREAS = [
  'demand', 'character', 'project', 'develop', 'jobitem', 'review',
  'material', 'fee', 'budget', 'quality', 'product', 'file', 'badge', 'mail'
];

const ENTRANCE_AREAS = {
  DEMAND: ['demand', 'badge'],
  CHARACTER: ['character', 'badge'],
  FUNCTION: ['character', 'badge'],
  SETUP: ['project', 'badge'],
  DEVELOP: ['develop', 'project', 'review', 'badge'],
  TEST: ['jobitem', 'quality', 'badge'],
  INTEGRATION: ['jobitem', 'project', 'badge'],
  MANHOUR: ['jobitem', 'quality', 'character', 'badge'],
  PROJECT: ['project', 'badge'],
  REVIEW: ['review', 'jobitem', 'badge'],
  SYSTEM: ['character', 'badge'],
  PRODUCT: ['product', 'badge'],
  SUBPROJECT: ['project', 'badge'],
  QUALITY: ['quality', 'badge'],
  BUDGET: ['budget', 'badge'],
  SUPPLYCHAIN: ['material', 'badge'],
  PROPERTY: ['product', 'badge']
};

let currentVm = null;
let stamp = 0;
const dirtyAt = Object.create(null);
const byEvent = new Map();
const areaHandlers = new Map();
const workspaceHandlers = new Set();
const queuedEvents = new Set();
let flushScheduled = false;

export function areaOf(event) {
  if (!event || typeof event !== 'string') {
    return null;
  }
  if (event.indexOf('close-') === 0) {
    return null;
  }
  if (event === 'waiting-for-approve-num' || event.indexOf('notify-') === 0) {
    return 'badge';
  }
  const prefixes = [
    ['refresh-quality-', 'quality'],
    ['refresh-bug-', 'quality'],
    ['refresh-review-', 'review'],
    ['refresh-budget-', 'budget'],
    ['refresh-material-', 'material'],
    ['refresh-fee-', 'fee'],
    ['refresh-develop-', 'develop'],
    ['refresh-test-', 'jobitem'],
    ['refresh-jobitem-', 'jobitem'],
    ['refresh-manhour-', 'jobitem'],
    ['refresh-product-', 'product'],
    ['refresh-demand-', 'demand'],
    ['refresh-project-', 'project'],
    ['refresh-subproject-', 'project'],
    ['refresh-setup-', 'project'],
    ['refresh-character-', 'character'],
    ['refresh-function-', 'character'],
    ['refresh-preproject-', 'character'],
    ['refresh-system-', 'character'],
    ['refresh-file-', 'file']
  ];
  for (let i = 0; i < prefixes.length; i++) {
    if (event.indexOf(prefixes[i][0]) === 0) {
      return prefixes[i][1];
    }
  }
  return null;
}

export function areasForEntrance(entrance) {
  return (ENTRANCE_AREAS[entrance] || ['jobitem', 'badge']).slice();
}

function bucket(event) {
  if (!byEvent.has(event)) {
    byEvent.set(event, new Set());
  }
  return byEvent.get(event);
}

function deliver(entry, payload) {
  const vm = entry.vm;
  if (vm && vm._inactive) {
    if (!vm.__wsPending) {
      vm.__wsPending = [];
    }
    const exists = vm.__wsPending.some(function (item) {
      return item.fn === entry.fn;
    });
    if (!exists) {
      vm.__wsPending.push({ fn: entry.fn, payload: payload });
    }
    return;
  }
  // 单个页面刷新出错不能中断其余订阅者
  try {
    entry.fn(payload);
  } catch (e) {
    console.error('[notify] handler failed', e);
  }
}

function notifyWorkspace(areas, message) {
  workspaceHandlers.forEach(function (entry) {
    deliver(entry, message);
  });
}

export function publish(payload) {
  const body = payload || {};
  const areas = [];
  (body.areas || []).forEach(function (area) {
    if (area && areas.indexOf(area) < 0) {
      areas.push(area);
    }
  });
  stamp += 1;
  areas.forEach(function (area) {
    dirtyAt[area] = stamp;
  });
  const message = { areas: areas, reason: body.reason || '', stamp: stamp };
  notifyWorkspace(areas, message);
  areas.forEach(function (area) {
    const set = areaHandlers.get(area);
    if (!set) {
      return;
    }
    set.forEach(function (entry) {
      deliver(entry, message);
    });
  });
  byEvent.forEach(function (set, event) {
    const area = areaOf(event);
    if (!area || areas.indexOf(area) < 0) {
      return;
    }
    set.forEach(function (entry) {
      deliver(entry, true);
    });
  });
  invalidateAreas(areas);
}

export function subscribe(topicOrAreas, handler) {
  const entry = { fn: handler, vm: currentVm };
  if (topicOrAreas === TOPIC || topicOrAreas === 'badge') {
    workspaceHandlers.add(entry);
    return function unsubscribe() {
      workspaceHandlers.delete(entry);
    };
  }
  const areas = Array.isArray(topicOrAreas) ? topicOrAreas : [topicOrAreas];
  areas.forEach(function (area) {
    if (!areaHandlers.has(area)) {
      areaHandlers.set(area, new Set());
    }
    areaHandlers.get(area).add(entry);
  });
  return function unsubscribe() {
    areas.forEach(function (area) {
      const set = areaHandlers.get(area);
      if (set) {
        set.delete(entry);
      }
    });
  };
}

function emitExact(event, payload) {
  const set = byEvent.get(event);
  if (!set) {
    return;
  }
  set.forEach(function (entry) {
    deliver(entry, payload);
  });
}

function flushQueued() {
  flushScheduled = false;
  const events = Array.from(queuedEvents);
  queuedEvents.clear();
  if (!events.length) {
    return;
  }
  const areas = [];
  events.forEach(function (event) {
    const area = areaOf(event);
    if (area && areas.indexOf(area) < 0) {
      areas.push(area);
    }
    emitExact(event, true);
  });
  stamp += 1;
  areas.forEach(function (area) {
    dirtyAt[area] = stamp;
  });
  const message = { areas: areas, reason: 'legacy-emit', stamp: stamp };
  notifyWorkspace(areas, message);
  areas.forEach(function (area) {
    const set = areaHandlers.get(area);
    if (!set) {
      return;
    }
    set.forEach(function (entry) {
      deliver(entry, message);
    });
  });
}

const bus = {
  $on: function (event, handler) {
    if (!event || typeof handler !== 'function') {
      return;
    }
    bucket(event).add({ fn: handler, vm: currentVm, event: event });
  },
  $off: function (event, handler) {
    if (!event) {
      byEvent.clear();
      return;
    }
    const set = byEvent.get(event);
    if (!set) {
      return;
    }
    if (typeof handler !== 'function') {
      byEvent.delete(event);
      return;
    }
    set.forEach(function (entry) {
      if (entry.fn === handler) {
        set.delete(entry);
      }
    });
  },
  $emit: function (event, payload) {
    const area = areaOf(event);
    if (area) {
      if (!payload) {
        return;
      }
      queuedEvents.add(event);
      if (!flushScheduled) {
        flushScheduled = true;
        Promise.resolve().then(flushQueued);
      }
      return;
    }
    emitExact(event, payload);
  },
  publish: publish,
  subscribe: subscribe,
  areasForEntrance: areasForEntrance
};

export function installNotify(Vue) {
  Vue.prototype.$bus = bus;
  Vue.mixin({
    created: function () {
      currentVm = this;
    },
    mounted: function () {
      currentVm = this;
    },
    activated: function () {
      const pending = this.__wsPending;
      if (!pending || !pending.length) {
        return;
      }
      this.__wsPending = [];
      pending.forEach(function (item) {
        item.fn(item.payload);
      });
    }
  });
}
