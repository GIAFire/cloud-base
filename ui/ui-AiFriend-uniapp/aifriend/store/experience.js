import { defineStore } from 'pinia';
import { experienceService, safeReturnRoute } from '../services/experience.js';
import router, { currentRoute } from '../router/index.js';
import { getMember, getAccessToken, MEMBER_CHANGED_EVENT } from '../helper/token.js';

export const notify = (title) => uni.showToast({ title, icon: 'none' });
const emptyRemoteData = () => ({
  favorites: [], followed: [], liked: [], saved: [], conversations: {}, comments: {},
});
export const useExperienceStore = defineStore('experience', {
  state: () => ({
    data: { ...emptyRemoteData(), ...experienceService.restore() },
    characters: [], clips: [], identity: getMember(), pending: {}, authInitialized: false,
    returnRoute: '', cancelRoute: '',
    discovery: { query: '', tag: '全部', category: '为你推荐' },
    feedTab: '推荐', feedIndex: 0, commentClip: '', profilePanel: '',
  }),
  getters: {
    loggedIn: (state) => Boolean(state.identity?.id != null && getAccessToken()),
    characterById: (state) => (id) => state.characters.find((person) => person.id === id),
  },
  actions: {
    initializeAuth() {
      if (this.authInitialized) return;
      this.authInitialized = true;
      uni.$on?.(MEMBER_CHANGED_EVENT, (member) => { this.identity = member; });
      if (getAccessToken()) this.refreshIdentity();
    },
    async refreshIdentity() {
      try { this.identity = await experienceService.restoreSession(); }
      catch { if (!getAccessToken()) this.identity = null; }
    },
    persist() {
      try { experienceService.save(this.data); }
      catch { notify('本地保存失败，请检查存储空间'); }
    },
    requireIdentity({ route = currentRoute() } = {}) {
      if (this.loggedIn) return true;
      const origin = safeReturnRoute(currentRoute());
      this.cancelRoute = origin.startsWith(router.routes.chat) ? router.routes.conversations : origin;
      this.returnRoute = safeReturnRoute(route);
      router.go(`${router.routes.login}?returnTo=${encodeURIComponent(this.returnRoute)}`);
      return false;
    },
    async authenticate(credentials, mode = 'login') {
      const member = await experienceService[mode === 'register' ? 'register' : 'login'](credentials);
      if (member?.id == null) throw new Error('登录失败，请稍后重试');
      this.identity = member;
      const target = safeReturnRoute(this.returnRoute);
      this.returnRoute = ''; this.cancelRoute = '';
      router.go(target, { reset: true });
      this.refreshIdentity();
    },
    cancelAuth() {
      const target = safeReturnRoute(this.cancelRoute || this.returnRoute);
      this.returnRoute = ''; this.cancelRoute = '';
      router.go(target, { reset: true });
    },
    async logout() {
      try {
        await experienceService.logout();
      } catch (error) { notify(error.message); }
      finally { this.identity = null; }
    },
    clear() {
      try {
        const local = experienceService.clear();
        this.data = { ...this.data, ...local };
        this.profilePanel = '';
        notify('本地草稿与偏好已清除');
        return true;
      } catch { notify('清除失败，请检查存储权限'); return false; }
    },
    async interact(key, id, type) {
      if (!this.requireIdentity()) return;
      try { await experienceService.updateInteraction({ type, id, active: !this.data[key]?.includes(id) }); }
      catch (error) { notify(error.message); }
    },
    openChat(id) {
      if (!this.characterById(id)) return;
      const route = `${router.routes.chat}?id=${encodeURIComponent(id)}`;
      if (this.requireIdentity({ route })) router.go(route);
    },
    saveDraft(key, id, value) {
      if (!['drafts', 'commentDrafts'].includes(key) || !id) return;
      this.data[key][id] = value; this.persist();
    },
    async send(id) {
      const text = (this.data.drafts[id] || '').trim();
      if (!text || this.pending[id] || !this.characterById(id) || !this.requireIdentity()) return;
      this.pending[id] = true;
      try { await experienceService.sendMessage({ characterId: id, text }); }
      catch (error) { notify(error.message); }
      finally { delete this.pending[id]; }
    },
    async addComment(id) {
      const text = (this.data.commentDrafts[id] || '').trim();
      if (!text || !this.requireIdentity()) return;
      try { await experienceService.addComment({ clipId: id, text }); }
      catch (error) { notify(error.message); }
    },
    setPreference(value) {
      this.data.preference = value; this.persist(); notify('已保存陪伴偏好');
    },
  },
});
export default useExperienceStore;
