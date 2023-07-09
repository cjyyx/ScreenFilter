## 依赖

https://github.com/halfhp/androidplot

## 软件使用逻辑

### 主界面

打开软件，打开准备界面（提示提供相应权限）（之后操作也要权限检查，不通过则放弃操作，且打开准备页面）

软件主界面，有

- 滤镜开关（支持状态栏快捷设置）
- 智能亮度开关（支持状态栏快捷设置）（关闭系统自动亮度，自己实现智能亮度）
- 最低硬件亮度拖动条（最低硬件亮度一般为手机屏幕关闭类 DC 调光的阈值）
- 最高滤镜不透明度拖动条，可以调整夜间全黑环境下最舒适的屏幕亮度
- 高光照阈值拖动条（光照达到阈值，打开系统自动亮度，使屏幕达到最大激发亮度）
- 亮度-光照曲线设置

### 光照控制亮度逻辑

通过亮度-光照曲线来实现。点击主界面的亮度-光照曲线设置按钮，打开亮度-光照曲线设置界面。可以通过增减修改光照-亮度对应点来调整亮度-光照曲线。

智能亮度逻辑通过有限状态机实现。有光照平稳、光照突增、光照突减、超高光照、用户调整这几种状态。

光照平稳、光照突增、光照突减：根据亮度-光照曲线设置屏幕亮度

高光照：打开系统自动亮度，使屏幕达到最大激发亮度

用户调整：在用户锁屏之前，或一定时间内，以用户调整的亮度为准



### 状态栏控制

状态栏快速设置服务，有屏幕滤镜开关、智能亮度开关、正常截图功能

软件正常运行时，应关闭系统自动亮度。

用户调整状态栏亮度调，会改变系统亮度，可以据此设置屏幕亮度。


## 项目架构、伪代码

### MainActivity, MainUI

主界面

#### 启动时

检查权限，打开准备界面

读取存储的应用设置或默认设置，保存到 `GlobalStatus` （应用存储变量，`BrightnessManager`）

### PreparatoryActivity

准备界面，使用户提供相应权限

### BrightnessPointActivity

用来设置亮度-光照曲线

通过 `GlobalStatus` 修改光照-亮度对应点列表，注意零光照点和高光照阈值点必须存在


### FilterViewManager

管理屏幕滤镜

### FilterAccessibilityService

无障碍服务，用户启用无障碍功能时被创建

#### 启动时

创建 `FilterViewManager`，加入到 `GlobalStatus`

#### 运行时

监视光照，更新至 `GlobalStatus`，光照改变时调用 `brightnessManager.onLightChanged(float light)`

监视系统亮度，更新至 `GlobalStatus`，用户改变系统亮度时调用 `brightnessManager.onSystemBrightnessChangedByUser(float brightness)`

### GlobalStatus

全局变量、方法

#### 应用存储变量

- minHardwareBrightness: 最低硬件亮度，当高于此亮度时，屏幕应为类 DC 调光
- maxFilterOpacity: 最高滤镜不透明度
- highLightThreshold: 高光照阈值


#### float light

当前传感器获得的光照强度，单位 lux

#### float userBrightness

当前用户设置的屏幕亮度，范围 [0,1]

#### void setFilterViewManager(FilterViewManager f)

#### void setBrightnessManager(BrightnessManager bm)

#### boolean isAccessibility()

判断是否具有足够的权限

#### void openPreparatoryActivity()

打开准备界面

#### void openFilter()

检查权限，打开滤镜

#### void closeFilter()

关闭滤镜

#### void setAlpha(float alpha)

调用 `FilterViewManager` 设置滤镜不透明度

`alpha` 取值 [0,1]， 0 表示完全透明，1 表示完全不透明

<!-- #### void setBrightness(float brightness)

设置亮度。在用户手动调整亮度时使用。

`brightness` 取值 [0,1]，0 表示最暗，1 表示最亮 -->

#### void openIntelligentBrightness()

检查权限，打开智能亮度

#### void closeIntelligentBrightness()

关闭智能亮度

#### list getBrightnessPointList()

返回光照-亮度对应点列表

#### void addBrightnessPoint(float light, float brightness)

添加光照-亮度对应点，同时更新应用存储

#### void delBrightnessPoint(int id)

删除光照-亮度对应点，同时更新应用存储

#### void setBrightnessPoint(int id, float light, float brightness)

设置光照-亮度对应点，同时更新应用存储

#### void onLightChanged(float light)

当传感器获取的光照强度改变时被调用，`light` 单位为 lux

当智能亮度开时，根据光照，计算得相应亮度，计算得相应系统亮度并设置，计算得相应滤镜不透明度并设置

#### void onSystemBrightnessChangedByUser(float brightness)

用户改变系统亮度时被调用

brightness 范围 [0,1]

### BrightnessManager

实现光照控制亮度逻辑

光照-亮度对应点 (光照强度{[0,+inf] lux}, 屏幕亮度{[0,1]})

### QuickSettingFilter

状态栏快速设置服务，开关屏幕滤镜

### QuickSettingScreenShot

状态栏快速设置服务，关闭屏幕滤镜，调用屏幕截图功能，再打开屏幕截图

### QuickSettingIntelligentBrightness

状态栏快速设置服务，开关智能亮度