中文 / [English](README.en.md)
<p align="left">
    <a href="https://opensource.org/licenses/Apache-2.0" alt="License">
        <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" /></a>
<a target="_blank" href="https://join.slack.com/t/neatlogichome/shared_invite/zt-1w037axf8-r_i2y4pPQ1Z8FxOkAbb64w">
<img src="https://img.shields.io/badge/Slack-Neatlogic-orange" /></a>
</p>

---

## 关于

neatlogic-event是事件管理模块，用于对事件的精细化管理，用于发起事件类工单，包含事件节点、事件类型管理、解决方案管理和事件处理等功能。

## 主要功能

### 事件节点

在流程管理中配置包含事件节点的流程<br>
![img.png](README_IMAGES/img.png)

- 支持通知功能
- 支持关联优先级的时效策略
- 支持指定处理人，或者配置分派器规则，如按分组成员工作量分配处理人

### 事件类型管理

事件类型是指事件归档的类型，事件类型还可以关联事件解决方案，事件类型有授权操作，授权的用户才能使用对应事件类型。
![img.png](README_IMAGES/img1.png)

### 解决方案管理

解决方案管理是将常见的事件的解决方案保存为模板，在事件处理过程中提供回复模板。
![img.png](README_IMAGES/img2.png)

- 解决方案支持关联多个事件类型

### 事件处理

在事件处理时，必须填写事件类型和解决方案，解决方案支持引用现成模板或者自定义。
![img.png](README_IMAGES/img3.png)

- 事件处理步骤支持暂停
- 在事件处理的过程中，支持添加新的解决方案。