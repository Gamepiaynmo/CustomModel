package com.github.gamepiaynmo.custommodel.api;

import com.github.gamepiaynmo.custommodel.server.CustomModel;
import com.github.gamepiaynmo.custommodel.server.selector.DefaultModelSelector;
import com.mojang.authlib.GameProfile;

import java.util.Collection;

/**
 * See {@link DefaultModelSelector} for an example implementation.
 */
public interface IModelSelector {
    /**
     * Invoked each time when a model needs to be chosen for a player.
     * Note that the player maybe offline.
     * @param profile Profile of the player.
     * @return Model ID of suggested model pack.
     */
    String getModelForPlayer(GameProfile profile);

    /**
     * Invoked each time when a model is selected by a player.
     * Note that the player maybe offline.
     * @param profile Profile of the player.
     * @param modelId Model ID of selected model pack.
     */
    void setModelForPlayer(GameProfile profile, String modelId);

    /**
     * Implement your own Model Selector to choose Model Packs for players.
     * Set to null to restore the default selector.
     * @param modelSelector New Model Selector.
     */
    public static void setModelSelector(IModelSelector modelSelector) {
        CustomModel.setModelSelector(modelSelector);
    }

    /**
     * Get the current Model Selector.
     * @return Current Model Selector.
     */
    public static IModelSelector getModelSelector() {
        return CustomModel.getModelSelector();
    }
}
