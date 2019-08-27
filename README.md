# CustomModel
Customize your player model in Minecraft using JSON.

#### Description

This mod allows you to customize your player model using JSON files. You can modify the appearance of each part of the model, add new boxes / planes to enrich your model. In addition, you can add ribbons to simulate hairs or scarfs, and also a brand-new customizable particle system!

#### Usage

After installation, JSON models are stored in a folder named "custom-models" under ".minecraft" (for servers, it is under the same folder as server.jar). Each model entry can either be a subfolder or a zip archive file. The name of the subfolder or zip file can be either the player name or player UUID (for example, my minecraft name is "Gamepiaynmo", and my UUID is "a94b57f9-13b8-4907-bd1e-ff622fcc5c5b", then the mod will try to find my model in the following four positions: folder "Gamepiaynmo", folder "a94b57f9-13b8-4907-bd1e-ff622fcc5c5b", file "Gamepiaynmo.zip" and file "a94b57f9-13b8-4907-bd1e-ff622fcc5c5b.zip", not case sensitive).

Each model entry must contain one main JSON file named "model.json". Supplement textures are all stored at the root of each folder or zip file, and must be stored in ".png" format. More information about the JSON schema, please refer to the [github wiki page](https://github.com/Gamepiaynmo/CustomModel/wiki/Schema) and some examples can be found at [here](https://github.com/Gamepiaynmo/CustomModel/tree/master/examples).

When rendering, the client will search for models both at client side and server side. The priority can be adjusted in configuration. Notice that model files at server side MUST be zip files. When the model file has changed, you can use command "/custommodel reload @p" to reload your model.

#### Contact

If you want some new features or need to report bugs, please rise a new issue at [github issue tracker](https://github.com/Gamepiaynmo/CustomModel/issues).



#### 说明

此模组允许您使用JSON文件自定义玩家模型。您可以修改模型的每个部分的外观，添加新的方块/平面以丰富您的模型。此外，您可以添加飘带来模拟头发或围巾，还可以添加全新的可定制粒子系统！

#### 用法

安装后，JSON模型存储在“.minecraft”文件夹下的名为“custom-models”的文件夹中（对于服务器，它与server.jar位于同一文件夹中）。每个模型条目可以是子文件夹或zip存档文件。子文件夹或zip文件的名称可以是玩家名称或玩家UUID（例如，我的名字是“Gamepiaynmo”，而我的UUID是“a94b57f9-13b8-4907-bd1e-ff622fcc5c5b”，那么该模组会尝试在以下四个位置找到我的模型：文件夹“Gamepiaynmo”，文件夹“a94b57f9-13b8-4907-bd1e-ff622fcc5c5b”，文件“Gamepiaynmo.zip”和文件“a94b57f9-13b8-4907-bd1e-ff622fcc5c5b.zip”，不区分大小写）。

每个模型条目必须包含一个名为“model.json”的主JSON文件。补充纹理都存储在每个文件夹或zip文件的根目录中，并且必须以“.png”格式存储。有关JSON模式的更多信息，请参阅[github wiki页面](https://github.com/Gamepiaynmo/CustomModel/wiki/Schema)，可以在[这里](https://github.com/Gamepiaynmo/CustomModel/tree/master/examples)找到一些示例。

渲染时，客户端将在客户端和服务器端搜索模型。可以在配置中调整优先级。请注意，服务器端的模型文件必须是zip文件。模型文件更改后，您可以使用命令“/custommodel reload @p”重新加载模型。

#### 联系

如果您需要一些新功能或需要报告错误，请在[github issue tracker](https://github.com/Gamepiaynmo/CustomModel/issues)上发布新问题。