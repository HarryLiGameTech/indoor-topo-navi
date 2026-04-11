# Data Annotation Task — SWFC 楼内导航地图

## 项目背景

上海环球金融中心（SWFC）是一栋超高层综合建筑，内设办公、酒店、观光等多种功能区域，各区域之间存在不同级别的通行限制（某些门需要刷卡才能通过）。

本项目为大楼室内导航系统建立拓扑图数据。**你的任务是将正确的通行限制（constraint）标注到对应的路径（atomic-path）和电梯站（station）上**，使系统能根据用户持有的卡片类型判断其能否进入特定区域。

---

## 文件结构说明

工作目录下有多类文件，请先通读本节再开始标注。

### `root` 文件（必读）

定义了本项目所有可用的 constraint 类型：

| Constraint | 需要持有 | 实际含义 |
|---|---|---|
| `TenantsOnly` | 员工卡（`haveStaffCard`） | 仅大厦住户/员工可通行，普通访客无法通过 |
| `ManagementOnly` | 管理卡（`haveManagementCard`） | 仅特定管理人员可通行 |
| `CanFitIntoPassengerElevator` | 总重量 < 200 kg | 限制客用电梯最大载重（通常无需手动标注） |

### 楼层地图文件（topo-map）

描述某一种楼层平面内部的空间结构，由以下两类元素组成：

- **`topo-node`**：空间中的一个位置节点（如电梯厅入口、走廊交叉口）
- **`atomic-path`**：连接两个节点的可通行路径
  - `<->` 表示**双向**通行
  - `->` 表示**单向**通行



### 交通工具文件（transport）

描述电梯、扶梯、楼梯等跨楼层设施，内容是各楼层停靠站（`station`）的列表。

> **如何判断**：若文件名是 SWFC 某台电梯/扶梯/楼梯的名称（如 `FS1_hi`、`HL`、`ST_U2`），则该文件一定是交通工具文件。

---

## 标注语法

### 标注 atomic-path

`requires` 关键字加在行末，`{cost = ...}` 等原有属性**保持不变**。

当 `<->` 双向路径中只有**一个方向**需要刷卡时，必须将其**拆分为两条单向路径**分别书写（注意：方向只能写 `->` 或 `<->`，**不能写 `<-`**）：

```
// Before（双向，无限制）
atomic-path [节点A <-> 节点B] {cost = 5}

// After ①：两个方向都需要刷卡
atomic-path [节点A -> 节点B] {cost = 5} requires TenantsOnly
atomic-path [节点B -> 节点A] {cost = 5} requires TenantsOnly

// After ②：仅 A→B 方向需要刷卡
atomic-path [节点A -> 节点B] {cost = 5} requires TenantsOnly
atomic-path [节点B -> 节点A] {cost = 5}
```

### 标注 station

同样在行末添加 `requires`：

```
// Before
station F51 at Floor51::L4_hall {location = 220.0, departureRate = 0.01}

// After
station F51 at Floor51::L4_hall {location = 220.0, departureRate = 0.01} requires TenantsOnly
```

---

## 标注规则

以下五类情况需要添加 constraint，请逐一核对每个文件。

---

### 规则 1：出消防前室 → `TenantsOnly`（单向）

**适用范围**：除 B1、1层、89层之外的**所有楼层**

消防前室是楼梯间与楼层主空间之间的隔离缓冲区。**从前室侧出来、进入楼层主区域**的方向需要刷卡（`TenantsOnly`）；反方向（从楼层主区域进入前室/楼梯）不需要刷卡。

相关节点名通常含有 `egress_hall`、`service_corr`、`hall_out` 等关键词，可用 Ctrl+F 搜索定位。

> **特别注意（96 层及以上）**：两台相邻消防电梯**共用同一个前室**。该共用前室与楼层主区域的连接路径只需标注一次，切勿重复。

```
// Before
atomic-path [service_corr_B <-> B_hall_out] {cost = 5}

// After（service_corr_B 在前室内部，出前室进入楼层主区域方向需刷卡）
atomic-path [service_corr_B -> B_hall_out] {cost = 5} requires TenantsOnly
atomic-path [B_hall_out -> service_corr_B] {cost = 5}
```

---

### 规则 2：从大堂/空中大堂进入分区电梯间 → `TenantsOnly`（单向）

**适用范围**：主大堂（1层）及空中大堂所在楼层：**28、29、52、53层**（对应文件 `SL28`、`SL29`、`SL52`、`SL53`）

从大堂公共区域进入分区电梯候梯厅方向需刷卡；从电梯厅返回大堂方向不需要。

```
// Before（in SL28）
atomic-path [G1_in <-> halls_entrance] {cost = 5}

// After（从大堂侧 G1_in 进入电梯厅 halls_entrance 方向需刷卡）
atomic-path [G1_in -> halls_entrance] {cost = 5} requires TenantsOnly
atomic-path [halls_entrance -> G1_in] {cost = 5}
```

---

### 规则 3：从 89 层离开 FS1/FS4 电梯间 → `ManagementOnly`（单向）

**适用范围**：仅 89 层（文件 `Floor89` 或对应文件，具体 atomic-path 请自行查找）

从 FS1 或 FS4 电梯厅离开、前往楼层其他区域的方向需要管理卡（`ManagementOnly`）；进入电梯厅方向不限制。

---

### 规则 4：79～88 层消防通道 → `ManagementOnly`（**双向**）

**适用范围**：79～88 层（这些楼层均为酒店客房层，复用文件 `GuestRoomFloor`）

此楼层段的两个消防通道**进出均需刷管理卡**，因此必须拆成两条单向路径，各自标注 `ManagementOnly`。

```
// Before（in GuestRoomFloor）
atomic-path [A_egress_hall_out <-> n_e0afebf32f8b474e9f1d831112adc7d8] {cost = 3}

// After（双向都需要刷卡，拆成两条）
atomic-path [A_egress_hall_out -> n_e0afebf32f8b474e9f1d831112adc7d8] {cost = 3} requires ManagementOnly
atomic-path [n_e0afebf32f8b474e9f1d831112adc7d8 -> A_egress_hall_out] {cost = 3} requires ManagementOnly
```

> 同理处理 `B_egress_hall_out` 侧对应的路径。

---

### 规则 5：客房电梯和办公分区电梯选层 → `TenantsOnly`（station）

**适用范围**：所有客房电梯和办公分区电梯，但**大堂层除外**

- 办公区电梯：大堂层为 **1/2/28/29/52/53 层**
- 酒店（客房）电梯：大堂层为 **87/91 层**

上述大堂层的 `station` 不需要标注；其余所有停靠站均需加 `requires TenantsOnly`，表示乘客选择该层时需要刷卡。

具体哪些楼层/哪些站需要标注，请参考 B 站上的电梯实录视频，以实际情况为准，**不要猜测**。

---

## 注意事项

1. **不确定是否要标？** 直接来问我，不要问 AI——AI 不了解这栋楼的实际情况，可能给出错误答案。
2. **以 B 站视频为准**：具体哪一层/哪个方向需要刷卡，均以视频中的实际观察为准。
3. **善用 Ctrl+F**：在文件内搜索 `egress_hall`、`service_corr`、`halls_entrance` 等关键词，可快速定位需要标注的路径。
4. **保留原有属性**：标注时只在行末追加 `requires XXX`，不要修改 `{cost = ...}` 等已有内容。
