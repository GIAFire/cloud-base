import {defineStore} from 'pinia';
import {characterById} from '../domain/catalog.js';
import {experienceService, replyFor, safeReturnRoute} from '../services/experience.js';
import router, {currentRoute} from '../router/index.js';

const replyTimers = new Map();
const message = (text, me) => ({
    id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
    text,
    me,
    createdAt: Date.now()
});
export const notify = (title) => uni.showToast({title, icon: 'none'});
export const useExperienceStore = defineStore('experience', {
    state: () => ({
        data: experienceService.restore(), pending: {}, returnRoute: '', cancelRoute: '', pendingAction: null,
        discovery: {query: '', tag: '全部', category: '为你推荐'},
        feedTab: '推荐', feedIndex: 0, commentClip: '', profilePanel: '',
    }),
    getters: {loggedIn: (state) => Boolean(state.data.identity)},
    actions: {
        persist() {
            try {
                experienceService.save(this.data);
            } catch {
                notify('本地保存失败，请检查存储空间；操作暂保留在当前页面');
            }
        },
        requireIdentity({route = currentRoute(), action = null} = {}) {
            if (this.loggedIn) return true;
            const origin = safeReturnRoute(currentRoute());
            this.cancelRoute = origin.startsWith(router.routes.chat) ? router.routes.conversations : origin;
            this.returnRoute = safeReturnRoute(route);
            this.pendingAction = action;
            router.go(`${router.routes.login}?returnTo=${encodeURIComponent(this.returnRoute)}`);
            return false;
        },
        authenticate(email) {
            this.data.identity = {email: email.trim().toLowerCase()};
            const action = this.pendingAction;
            this.pendingAction = null;
            if (action?.type === 'favorite') this.toggle('favorites', action.id);
            if (action?.type === 'follow') this.toggle('followed', action.id);
            if (action?.type === 'like') this.toggle('liked', action.id);
            if (action?.type === 'save') this.toggle('saved', action.id);
            if (action?.type === 'chat') this.startConversation(action.id);
            if (action?.type === 'preference') {
                this.data.preference = action.value;
                this.profilePanel = 'preference';
            }
            if (action?.type === 'plus') {
                this.data.plusReserved = true;
                notify('已加入体验候补名单');
            }
            this.persist();
            const target = safeReturnRoute(this.returnRoute);
            this.returnRoute = '';
            this.cancelRoute = '';
            router.go(target, {reset: true});
        },
        cancelAuth() {
            this.pendingAction = null;
            const target = safeReturnRoute(this.cancelRoute || this.returnRoute);
            this.returnRoute = '';
            this.cancelRoute = '';
            router.go(target, {reset: true});
        },
        logout() {
            this.cancelReplies();
            this.data.identity = null;
            this.pendingAction = null;
            this.persist();
            notify('已退出演示登录，体验数据已保留');
        },
        clear() {
            let initial;
            try {
                initial = experienceService.clear();
            } catch {
                notify('清除失败，请检查浏览器存储权限后重试');
                return;
            }
            this.cancelReplies();
            this.data = initial;
            this.pendingAction = null;
            this.returnRoute = '';
            this.discovery = {query: '', tag: '全部', category: '为你推荐'};
            this.feedTab = '推荐';
            this.feedIndex = 0;
            this.commentClip = '';
            this.profilePanel = '';
            this.cancelRoute = '';
            notify('全部本地体验数据已清除');
        },
        toggle(key, id) {
            const values = this.data[key];
            if (!Array.isArray(values)) return;
            this.data[key] = values.includes(id) ? values.filter((value) => value !== id) : [...values, id];
            this.persist();
        },
        interact(key, id, type) {
            if (!this.requireIdentity({action: {type, id}})) return;
            this.toggle(key, id);
        },
        openChat(id) {
            if (!characterById(id)) return;
            const route = `${router.routes.chat}?id=${id}`;
            if (!this.requireIdentity({route, action: {type: 'chat', id}})) return;
            this.startConversation(id);
            router.go(route);
        },
        startConversation(id) {
            const person = characterById(id);
            if (!person || this.data.conversations[id]) return;
            this.data.conversations[id] = [message(person.quote, false)];
            this.persist();
        },
        saveDraft(key, id, value) {
            this.data[key][id] = value;
            this.persist();
        },
        send(id) {
            const value = (this.data.drafts[id] || '').trim();
            if (!value || this.pending[id] || !characterById(id)) return;
            if (!this.requireIdentity()) return;
            this.startConversation(id);
            this.data.conversations[id].push(message(value, true));
            this.data.drafts[id] = '';
            this.pending[id] = true;
            this.persist();
            const reply = replyFor(value, this.data.preference);
            replyTimers.set(id, setTimeout(() => {
                if (this.data.conversations[id]) this.data.conversations[id].push(message(reply, false));
                delete this.pending[id];
                replyTimers.delete(id);
                this.persist();
            }, 700));
        },
        cancelReplies() {
            replyTimers.forEach(clearTimeout);
            replyTimers.clear();
            this.pending = {};
        },
        addComment(id) {
            const value = (this.data.commentDrafts[id] || '').trim();
            if (!value || !this.requireIdentity()) return;
            (this.data.comments[id] ||= []).push(message(value, true));
            this.data.commentDrafts[id] = '';
            this.persist();
        },
        setPreference(value) {
            if (!this.requireIdentity({action: {type: 'preference', value}})) return;
            this.data.preference = value;
            this.persist();
            notify('已保存陪伴偏好');
        },
    },
});
export default useExperienceStore;
