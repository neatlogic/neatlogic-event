[中文](README.md) / English
<p align="left">
    <a href="https://opensource.org/licenses/Apache-2.0" alt="License">
        <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" /></a>
<a target="_blank" href="https://join.slack.com/t/neatlogichome/shared_invite/zt-1w037axf8-r_i2y4pPQ1Z8FxOkAbb64w">
<img src="https://img.shields.io/badge/Slack-Neatlogic-orange" /></a>
</p>

---

## About

Neatlogic-event is a module used to initiate an event work order, including the event node, the event type management
the solution of event and the event handling method.

## Feature

### Event-node

On the Process Management page, add a process that contains the event node.<br>
![img.png](README_IMAGES/img.png)

- Support informs them of their functionality
- Supports time-sensitive policies that associate priorities
- Support for specifying agents, or configuring dispatcher rules, such as assign agents by group member workload.

### Event type management

The event type refers to the type to which the event belongs，event type can relate to solution of event. Event types
have authorization capabilities，and just authorized users can use the event mode.
![img.png](README_IMAGES/img1.png)

### Solution of event

Solution management is the process of saving common event solutions as templates and providing response templates during
event processing.
![img.png](README_IMAGES/img2.png)

- The solution supports associating multiple event types.

### Event handling

When handling events, that must fill in the event type and solution. The solution supports selecting templates or
customizing.
![img.png](README_IMAGES/img3.png)

- event node steps support pause and resume processing.
- Support the addition of new solutions during event processing.