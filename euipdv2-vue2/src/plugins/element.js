/*
 * Element UI 按需注册（配合 babel-plugin-component）
 * 仅打包业务实际用到的组件，避免全量 theme-chalk/index.css
 */
import Vue from 'vue';
import {
  Alert,
  Aside,
  Autocomplete,
  Badge,
  Button,
  ButtonGroup,
  Card,
  Cascader,
  Checkbox,
  CheckboxGroup,
  Col,
  Collapse,
  CollapseItem,
  Container,
  DatePicker,
  Dialog,
  Divider,
  Drawer,
  Dropdown,
  DropdownItem,
  DropdownMenu,
  Footer,
  Form,
  FormItem,
  Header,
  Image,
  Input,
  InputNumber,
  Link,
  Loading,
  Main,
  Message,
  MessageBox,
  Notification,
  Option,
  OptionGroup,
  Pagination,
  Popover,
  Progress,
  Radio,
  RadioButton,
  RadioGroup,
  Rate,
  Row,
  Select,
  Steps,
  Step,
  Switch,
  TabPane,
  Table,
  TableColumn,
  Tabs,
  Tag,
  TimePicker,
  Timeline,
  TimelineItem,
  Tooltip,
  Transfer,
  Tree,
  Upload
} from 'element-ui';

const components = [
  Alert, Aside, Autocomplete, Badge, Button, ButtonGroup, Card, Cascader,
  Checkbox, CheckboxGroup, Col, Collapse, CollapseItem, Container, DatePicker,
  Dialog, Divider, Drawer, Dropdown, DropdownItem, DropdownMenu, Footer, Form,
  FormItem, Header, Image, Input, InputNumber, Link, Main,
  Option, OptionGroup, Pagination, Popover, Progress, Radio, RadioButton,
  RadioGroup, Rate, Row, Select, Steps, Step, Switch, TabPane, Table,
  TableColumn, Tabs, Tag, TimePicker, Timeline, TimelineItem, Tooltip,
  Transfer, Tree, Upload
];

components.forEach((comp) => {
  if (comp && comp.name) {
    Vue.component(comp.name, comp);
  }
});

Vue.use(Loading.directive);

Vue.prototype.$loading = Loading.service;
Vue.prototype.$msgbox = MessageBox;
Vue.prototype.$alert = MessageBox.alert;
Vue.prototype.$confirm = MessageBox.confirm;
Vue.prototype.$prompt = MessageBox.prompt;
Vue.prototype.$notify = Notification;
Vue.prototype.$message = Message;

export default {
  size: 'small',
  zIndex: 300
};
