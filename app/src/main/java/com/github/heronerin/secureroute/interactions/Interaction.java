package com.github.heronerin.secureroute.interactions;

import static com.github.heronerin.secureroute.interactions.Interaction.InteractionVariety.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/*
    Interactions are the most basic event of unit of the SecureRoute system.
    One Interaction means nothing, and inorder to do things they all are computed.
    As such multiple Interactions can be made between devices and then can
    be combined into one event history.
    Interactions simply hold a JSONObject, and can't be edited. Interactions
    can however can have separate revision Interactions that modify the json
    of a specific interaction.
    Then are parsed into proper SecureRoute events.
 */

public class Interaction {
    public static enum InteractionVariety{
        INTERACTION,
        INTERACTION_REVISION
    }
    private JSONObject jsonObject;
    private InteractionVariety variety;
    Interaction(JSONObject _jsonObject, InteractionVariety _variety){
        jsonObject = _jsonObject;
        variety = _variety;
    }


    InteractionVariety getVariety(){ return variety; }
    JSONObject getJson(){ return jsonObject; }
    void setJson(JSONObject _jsonObject){
        jsonObject=_jsonObject;
    }


    public static JSONObject patchLevel(JSONObject patchFrom, JSONObject patchTo) throws JSONException {
        for (Iterator<String> it = patchFrom.keys(); it.hasNext(); ) {
            String key = it.next();
            if (patchFrom.get(key) instanceof JSONObject
                    && patchTo.has(key)
                    && patchTo.get(key) instanceof JSONObject) {
                patchTo.put(key, patchLevel(patchFrom.getJSONObject(key), patchTo.getJSONObject(key)));
            } else if (!patchFrom.isNull(key)) {
                patchTo.put(key, patchFrom.get(key));
            }
        }
        return patchTo;
    }

    // This only works for INTERACTION_REVISION
    public void patch(Interaction other) throws JSONException, NullPointerException {
        if (getVariety() != INTERACTION_REVISION || other.getVariety() != INTERACTION)
            throw new VerifyError("Incorrect use of patch, must be called on INTERACTION_REVISION with a normal INTERACTION as the argument");

        if (this.getJson() == null || other.getJson() == null)
            throw new NullPointerException("One or both of the Interactions have no Json!");

        other.setJson(
                patchLevel(this.getJson(), other.getJson())
        );
    }



}
