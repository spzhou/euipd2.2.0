/*
 * Author: 周朔鹏
 * Date: 2025-08-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 */

import Vue from "vue"
import Router from "vue-router"

Vue.use(Router);

/** 路由懒加载：登录/注册轻量首屏；业务页按需分包 */
const Login = () => import(/* webpackChunkName: "auth" */ "./views/auth/login.vue")
const Admin = () => import(/* webpackChunkName: "admin-shell" */ "./views/shell/admin-v2.vue")

export default new Router({
    mode: "history",
    base: process.env.BASE_URL,
    routes: [
        {
            path: "*",
            redirect: "/login",
        }, {
            path: "",
            redirect: "/login",
        }, {
            path: "/login",
            component: Login
        }, {
            path: "/",
            name: "admin",
            component: Admin,
            meta: {
                loginRequire: true
            },
            children: [{
                path: "welcome",
                name: "welcome",
                component: () => import(/* webpackChunkName: "welcome" */ "@/views/rdms/welcome.vue"),
                meta: { keepAlive: true }
            }, {
                path: "profile",
                name: "profile",
                component: () => import(/* webpackChunkName: "system" */ "@/views/rdms/system/profile"),
            }, {
                path: "system/customer",
                name: "system/customer",
                component: () => import(/* webpackChunkName: "system" */ "@/views/rdms/system/customer"),
            }, {
                path: "system/customer-role",
                name: "system/customer-role",
                component: () => import(/* webpackChunkName: "system" */ "./views/rdms/system/role.vue"),
            }, {
                path: "system/tenant-ui-policy",
                name: "system/tenant-ui-policy",
                component: () => import(/* webpackChunkName: "system" */ "@/views/rdms/system/tenant-ui-policy.vue"),
            }, {
                path: "system/developer",
                name: "system/developer",
                component: () => import(/* webpackChunkName: "system" */ "@/views/rdms/system/developer"),
            }, {
                path: "system/general",
                name: "system/general",
                component: () => import(/* webpackChunkName: "system" */ "@/views/rdms/system/general"),
            }, {
                path: "system/product",
                name: "system/product",
                component: () => import(/* webpackChunkName: "system" */ "@/views/rdms/system/product"),
            }, {
                path: "system/approve",
                name: "system/approve",
                component: () => import(/* webpackChunkName: "system" */ "@/views/rdms/system/approve"),
            }, {
                path: "customer/setup",
                name: "customer/setup",
                component: () => import(/* webpackChunkName: "setup" */ "@/views/rdms/research/setup"),
                meta: { keepAlive: true }
            }, {
                path: "customer/project",
                name: "customer/project",
                component: () => import(/* webpackChunkName: "project" */ "@/views/rdms/research/project"),
                meta: { keepAlive: true }
            }, {
                path: "customer/character",
                name: "customer/character",
                component: () => import(/* webpackChunkName: "character" */ "@/views/rdms/research/character"),
                meta: { keepAlive: true }
            }, {
                path: "customer/module",
                name: "customer/module",
                component: () => import(/* webpackChunkName: "module" */ "@/views/rdms/research/module.vue"),
            }, {
                path: "customer/develop",
                name: "customer/develop",
                component: () => import(/* webpackChunkName: "develop" */ "@/views/rdms/research/develop"),
                meta: { keepAlive: true }
            }, {
                path: "customer/review",
                name: "customer/review",
                component: () => import(/* webpackChunkName: "review" */ "@/views/rdms/research/review.vue"),
            }, {
                path: "customer/demand",
                name: "customer/demand",
                component: () => import(/* webpackChunkName: "demand" */ "@/views/rdms/research/demand"),
                meta: { keepAlive: true }
            }, {
                path: "customer/quality",
                name: "customer/quality",
                component: () => import(/* webpackChunkName: "quality" */ "@/views/rdms/execute/quality"),
            }, {
                path: "customer/qualitydefine",
                name: "customer/qualitydefine",
                component: () => import(/* webpackChunkName: "quality" */ "@/views/rdms/execute/qualitydefine.vue"),
            }, {
                path: "customer/product",
                name: "customer/product",
                component: () => import(/* webpackChunkName: "product" */ "@/views/rdms/research/product"),
            }, {
                path: "customer/supplychain",
                name: "customer/supplychain",
                component: () => import(/* webpackChunkName: "supplychain" */ "@/views/rdms/supply/supplychain"),
            }, {
                path: "customer/material",
                name: "customer/material",
                component: () => import(/* webpackChunkName: "material" */ "@/views/rdms/supply/material.vue"),
                meta: { keepAlive: true }
            }, {
                path: "customer/performance",
                name: "customer/performance",
                component: () => import(/* webpackChunkName: "performance" */ "@/views/rdms/operate/performance.vue"),
            }, {
                path: "customer/jobitem",
                name: "customer/jobitem",
                component: () => import(/* webpackChunkName: "jobitem" */ "@/views/rdms/execute/jobitem.vue"),
            }, {
                path: "customer/worker",
                name: "customer/worker",
                component: () => import(/* webpackChunkName: "worker" */ "@/views/rdms/operate/worker"),
            }, {
                path: "customer/work-calendar",
                name: "customer/work-calendar",
                component: () => import(/* webpackChunkName: "worker" */ "@/views/rdms/operate/work-calendar.vue"),
            }, {
                path: "customer/mail/:folder?",
                name: "customer/mail",
                component: () => import(/* webpackChunkName: "mail" */ "@/views/rdms/mail/mail-frame.vue"),
                meta: { keepAlive: true }
            }, {
                path: "customer/mail-compose",
                name: "customer/mail-compose",
                component: () => import(/* webpackChunkName: "mail" */ "@/views/rdms/mail/mail-frame.vue"),
                meta: { keepAlive: true }
            }, {
                path: "customer/course",
                name: "customer/course",
                component: () => import(/* webpackChunkName: "course" */ "@/views/rdms/operate/course.vue"),
            }, {
                path: "customer/manhour",
                name: "customer/manhour",
                component: () => import(/* webpackChunkName: "manhour" */ "@/views/rdms/execute/manhour"),
                meta: { keepAlive: true }
            }, {
                path: "customer/test",
                name: "customer/test",
                component: () => import(/* webpackChunkName: "test" */ "@/views/rdms/research/test"),
            }, {
                path: "customer/function",
                name: "customer/function",
                component: () => import(/* webpackChunkName: "function" */ "@/views/rdms/research/function.vue"),
            }, {
                path: "customer/budget",
                name: "customer/budget",
                component: () => import(/* webpackChunkName: "budget" */ "@/views/rdms/operate/budget"),
            }, {
                path: "file/file",
                name: "file/file",
                component: () => import(/* webpackChunkName: "file" */ "./views/rdms/file/file.vue"),
            }, {
                path: "customer/download",
                name: "customer/download",
                component: () => import(/* webpackChunkName: "download" */ "@/views/rdms/operate/download.vue"),
            }, {
                path: "customer/warehouse",
                name: "customer/warehouse",
                component: () => import(/* webpackChunkName: "warehouse" */ "@/views/rdms/supply/warehouse.vue"),
            }, {
                path: "customer/inventory",
                name: "customer/inventory",
                component: () => import(/* webpackChunkName: "inventory" */ "@/views/rdms/supply/inventory.vue"),
            }, {
                path: "customer/summary",
                name: "customer/summary",
                component: () => import(/* webpackChunkName: "summary" */ "@/views/rdms/operate/summary-frame.vue"),
            }, {
                path: "customer/flow",
                name: "customer/flow",
                component: () => import(/* webpackChunkName: "flow" */ "@/views/rdms/flow/flow.vue"),
            }
            ]
        }]
})
