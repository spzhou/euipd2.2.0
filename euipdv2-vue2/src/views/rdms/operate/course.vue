<!--
 * Author: 周朔鹏
 * Date: 2025-04-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <!--Tab 卡片-->
    <div class="row">
      <div class="col-md-12">
        <div class="tabbable">
          <ul class="nav nav-tabs" id="myTab">
            <li :class="{active : activePageId==='course'}">
              <a @click="tabCourseClick">
                <i class="green ace-icon fa fa-home bigger-120"></i>
                课程管理
              </a>
            </li>
            <li :class="{active : activePageId==='home'}">
              <a @click="tabHomeClick">
                分类管理
              </a>
            </li>

            <!--篇章管理-->
            <li v-for="(item, index) in courseItemList"
                :class="{active : activePageId===('course-item-' + (index))}">
              <a @click="tabCourseItemClick(index)">
                <TabButton
                    :key="('course-item-' + (index))"
                    :index-in="index"
                    :close-function="closeFunction_course"
                >{{ ('课程篇章 : ' + (item.name)) }}
                </TabButton>
              </a>
            </li>

            <!--小节管理-->
            <li v-for="(item, index) in chapterItemList"
                :class="{active : activePageId===('chapter-item-' + (index))}">
              <a @click="tabChapterItemClick(index)">
                <TabButton
                    :key="('chapter-item-' + (index))"
                    :index-in="index"
                    :close-function="closeFunction_chapter"
                >{{ ('篇章小节 : ' + (item.name)) }}
                </TabButton>
              </a>
            </li>
          </ul>

          <div class="tab-content">
            <div id="home" class="tab-pane fade"
                 :class="{active : activePageId === 'home', in : activePageId === 'home' }">
              <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                <div>
                  <p>课程分类管理: 对课程的分类进行管理，包括一级分类和二级分类. </p>
                </div>
              </div>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <Category></Category>
              </div>
            </div>
            <div id="course" class="tab-pane fade"
                 :class="{active : activePageId === 'course', in : activePageId === 'course' }">
              <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                <div>
                  <p>课程管理: 新增课程以及对课程进行章节编辑等操作的管理页面. </p>
                </div>
              </div>
              <div class="hr hr-18 dotted hr-double"></div>
              <div>
                <CourseManage
                    :open-chapter="openChapter"
                ></CourseManage>
              </div>
            </div>
            <!-- 篇章管理-->
            <div v-for="(item, index) in courseItemList"
                 :key="('course-item-' + (item.id))"
                 v-bind:id="('course-item-' + (index))"
                 class="tab-pane fade"
                 :class="{active : activePageId === ('course-item-' + (index)), in : activePageId === ('course-item-' + (index)) }">
              <div>
                <Chapter
                    :key="item.id"
                    :course="item"
                    :open-section="openSection"
                ></Chapter>
              </div>
            </div>
            <!-- 小节管理-->
            <div v-for="(item, index) in chapterItemList"
                 :key="('chapter-item-' + (item.id))"
                 v-bind:id="('chapter-item-' + (index))"
                 class="tab-pane fade"
                 :class="{active : activePageId === ('chapter-item-' + (index)), in : activePageId === ('chapter-item-' + (index)) }">
              <div v-if="item && item.id">
                <ChapterSection
                    :key="item.id"
                    :chapter-id="item.id"
                ></ChapterSection>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import Category from "@/components/course/category.vue";
import CourseManage from "@/components/course/course-manage.vue";
import TabButton from "@/components/rdms/tab-button.vue";
import DevelopCharacterTaskTab from "@/components/rdms/develop-character-task-tab.vue";
import Chapter from "@/components/course/chapter.vue";
import ChapterCourseSection from "@/components/course/course-section.vue";

export default {
  components: {
    Chapter,
    ChapterSection: ChapterCourseSection,
    DevelopCharacterTaskTab,
    TabButton,
    Category,
    CourseManage,
  },

  name: "course",

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      activePageId: 'course',
      savedPageId: 'course',
      courseItemList: [],
      chapterItemList: [],
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
  },

  computed: {},

  methods: {
    getLoginUser() {
      let _this = this;
      if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer = Tool.getLoginUser();
      } else {
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
    },

    tabHomeClick() {
      let _this = this;
      _this.activePageId = 'home';
      _this.savedPageId = 'home';
    },
    tabCourseClick() {
      let _this = this;
      _this.activePageId = 'course';
      _this.savedPageId = 'course';
    },

    tabCourseItemClick(i) {
      let _this = this;
      _this.activePageId = `course-item-${i}`;
      _this.savedPageId = `course-item-${i}`;
    },
    openChapter(course) {
      let _this = this;
      let flag = false;
      let indexFlag = 0;
      _this.courseItemList.forEach((courseItem, index) => {
        if (courseItem.id === course.id) {
          flag = true;
          indexFlag = index;
        }
      })
      if (flag === false) {
        _this.courseItemList.push(course);
        _this.activePageId = `course-item-${_this.courseItemList.length - 1}`;
      } else {
        _this.activePageId = `course-item-${indexFlag}`;
      }
    },
    closeFunction_course(index) {
      let _this = this;
      _this.courseItemList.splice(index, 1);
      index--;
      if (index >= 0) {
        _this.activePageId = `course-item-${index}`;
      } else {
        _this.activePageId = 'course';
      }
    },
    tabChapterItemClick(i) {
      let _this = this;
      _this.activePageId = `chapter-item-${i}`;
    },
    openSection(chapter) {
      let _this = this;
      let flag = false;
      let indexFlag = 0;
      _this.chapterItemList.forEach((chapterItem, index) => {
        if (chapterItem.id === chapter.id) {
          flag = true;
          indexFlag = index;
        }
      })
      if (flag === false) {
        _this.chapterItemList.push(chapter);
        _this.activePageId = `chapter-item-${_this.chapterItemList.length - 1}`;
      } else {
        _this.activePageId = `chapter-item-${indexFlag}`;
      }
    },
    closeFunction_chapter(index) {
      let _this = this;
      _this.chapterItemList.splice(index, 1);
      index--;
      if (index >= 0) {
        _this.activePageId = `chapter-item-${index}`;
      } else {
        _this.activePageId = _this.savedPageId;
      }
    },

  }
}
</script>
