/**
 * 供新写或改写的方法使用。返回可观察的 loading / error，以及 run(task)。
 */
import Vue from 'vue';

export function usePage() {
  const state = Vue.observable({
    loading: false,
    error: ''
  });

  async function run(task) {
    state.loading = true;
    state.error = '';
    try {
      return await task();
    } catch (error) {
      state.error = (error && error.message) || '请求失败';
      throw error;
    } finally {
      state.loading = false;
    }
  }

  return { state: state, run: run };
}
