## CustomModel

Customize your player model in Minecraft using JSON.

![icon](https://github.com/Gamepiaynmo/CustomModel/wiki/assets/icon.png)

#### Description

This mod allows you to customize your player model using JSON files while maintaining compatibility with vanilla Minecraft features as well as other mods. The combination of expressions, physics and the new particle system enables unbelievable customizability!

#### Installing

The fabric version needs fabric-api as dependency. Mod Menu is also recommended as it enables adjusting configurations using graphical interface.

After installation, put your JSON models in the folder named "custom-models" under ".minecraft" (for servers, it is under the same folder as server.jar). Each model entry can either be a subfolder or a zip archive file. When in game, use the command /custommodel select "model id" to choose a model for you, or you can assign a default model in the configuration.

#### Modeling

Each model entry is either a folder or a zip archive file. It must contain one main JSON file named "model.json". Supplement textures are all stored at the root, and must be stored in PNG format. More information about the JSON schema, please refer to the [github wiki page](https://github.com/Gamepiaynmo/CustomModel/wiki/Schema) and some examples can be found [here](https://github.com/Gamepiaynmo/CustomModel/tree/master/examples).

#### Command

/custommodel is the only command that the mod added to the game. It has 4 sub-commands:

- reload: Reload the model from disk files. When you modify the models on the disk, you can use this command to reload them.
- refresh: Force the mod to refresh to model list when you add new models to the folder.
- select xxx: Select the model with id xxx for the player.
- list: List all models that the mod loaded. Then you can click on the message returned by the command to get the model selecting command.
- clear: Clear the current model of the player.

#### FAQ

**I want some new features / find some bugs!**

You can rise new issues at [github issue tracker](https://github.com/Gamepiaynmo/CustomModel/issues). Or you can contact me at [Custom Player Model Discord](https://discord.gg/uVT39n5).

**Is this mod client side or server side?**

I would say both. You can have this only at client side so that only yourself can see custom models. Or you can have a modded server which enables every player with this mod to see the models.

#### Acknowledgements

- This mod is inspired by [Optifine](https://optifine.net/) and [More Player Models](https://www.curseforge.com/minecraft/mc-mods/more-player-models).
- Some of the models come from [Touhou Little Maid](https://www.curseforge.com/minecraft/mc-mods/touhou-little-maid) mod.
- Minecraft modeling software [Blockbench](https://www.blockbench.net/web/).
- [ASMHelper](https://github.com/squeek502/ASMHelper) from [squeek502](https://github.com/squeek502) assisting Core Mod development.



#### 说明

这个mod允许您使用JSON文件自定义玩家模型，同时保持与原版Minecraft功能以及其他mod的兼容性。表达式，物理系统和粒子系统的结合可以达到难以置信的可定制程度！

#### 安装

Fabric版本需要fabric-api模组作为依赖项。此外建议使用Mod Menu模组，因为它可以使用图形界面调整配置。

安装完成后，请将JSON模型放在“.minecraft”下名为“custom-models”的文件夹中（对于服务器，它与server.jar在同一文件夹下）。 每个模型包都可以是子文件夹或zip压缩文件。 在游戏中时，使用命令/custommodel select “model id”为您自己选择一个模型，或者您可以在配置中分配一个默认模型。

#### 建模

每个模型包都是一个文件夹或一个zip存档文件，它必须包含一个名为“model.json”的主JSON文件。贴图文件均存储于模型包根目录，并且必须以PNG格式存储。 有关JSON模式的更多信息，请参考[github wiki页面](https://github.com/Gamepiaynmo/CustomModel/wiki/Schema)，并可以在[这里](https://github.com/Gamepiaynmo/CustomModel/tree/master/examples)找到一些示例。

#### 指令

/custommodel是该mod添加到游戏的唯一指令。它具有4个子指令：

- reload：从磁盘文件中重新加载模型。 当修改模型文件后，可以使用此命令重新加载它们。
- refresh：将新模型添加到文件夹时，强制Mod刷新模型列表。
- select xxx：为玩家选择ID为xxx的模型。
- list：列出mod加载的所有模型。 然后，您可以单击命令返回的消息以获取模型选择命令。

#### 常见问题

**我想要一些新功能/报告一些bug！**

您可以在[github](https://github.com/Gamepiaynmo/CustomModel/issues)提出新问题。 或者，您可以通过[Discord](https://discord.gg/uVT39n5)与我联系。

**这是客户端mod还是服务器mod？**

两者都是。 您可以只在客户端使用它，这样只有您自己才能看到自定义模型。 或者，您也可以在服务器使用，使每个安装过此Mod的每个玩家都能看到新模型。

#### 致谢

- 模组的灵感来源于[Optifine](https://optifine.net/)和[更多玩家模型](https://www.curseforge.com/minecraft/mc-mods/more-player-models)。

- 部分模型来源于模组[车万女仆](https://www.curseforge.com/minecraft/mc-mods/touhou-little-maid)。

- 方块风格建模软件[Blockbench](https://www.blockbench.net/web/)。

- [squeek502](https://github.com/squeek502)编写的[ASMHelper](https://github.com/squeek502/ASMHelper)辅助Core Mod开发。