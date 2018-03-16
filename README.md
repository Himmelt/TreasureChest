# TreasureChest

### 指令
```
/treasure lang [zh_cn,en_us,...] 查看/设置语言
/treasure save 保存配置到文件
/treasure reload 从文件重新载入配置
/treasure create 在左键选择的位置创建宝箱(配置是默认的，需要手动修改配置文件后重载)
/treasure open 在左键选择的位置打开宝箱仓库配置
```

### 配置
```yaml
# 语言
lang: zh_cn
# 宝箱
boxes:
  # 宝箱唯一id，也是坐标，世界名尽量短且不要带"."
  world,0,100,0:
    # 打开宝箱后再次刷新的时间
    refresh: 100
    # 从仓库随机生成到宝箱的物品数量
    rand_amount: 5
    # 仓库的大小，行数，每行9个格子
    line_amount: 6
    # 是否独占，一个玩家打开宝箱时，其他玩家无法打开(未实现)
    engross: false
    # 是否覆盖，如果目标位置已经存在其他方块，是否用宝箱覆盖
    override: true
    # 是否消失，关闭宝箱后，宝箱是否消失
    disappear: true
    # 是否广播，玩家打开宝箱时是否广播(未实现)
    broadcast: false
```

### 权限
```yaml
permissions:
  # 管理权限，执行/treasure 命令需要
  treasure.admin:
    default: op
  # 使用权限，拥有才权限才可以打开宝箱(默认：false)
  treasure.use:
    default: false
```

TODO: plugin.yml 添加子命令
