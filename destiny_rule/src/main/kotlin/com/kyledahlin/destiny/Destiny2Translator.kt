package com.kyledahlin.destiny

import com.kyledahlin.utils.getObject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import javax.inject.Inject

private const val MOD_COMPONENT_HASH = "4046539562"

/**
 * Parse the json responses from the bungie api
 */
open class Destiny2Translator @Inject constructor() {

    private val basicJson = Json

    open fun getModHashesFromResponse(response: String): List<String> {
        val root = basicJson.decodeFromString<JsonObject>(response)
        val data = root.getObject("Response").getObject("sales").getObject("data")

        return data.keys
            .map { data.getObject(it) }
            .filter {
                val costArray = it["costs"] as JsonArray
                costArray.any { costBlockJson ->
                    val costBlock = costBlockJson as JsonObject
                    costBlock["itemHash"]!!.jsonPrimitive.content == MOD_COMPONENT_HASH && costBlock["quantity"]!!.jsonPrimitive.int == 10
                }
            }.map {
                it["itemHash"]!!.jsonPrimitive.content
            }
    }

    /**
     * Return the english name and the path to the mod image. The Url is not a complete link and must be appended
     * to the base bungie api
     */
    open fun getNameAndImageForModResponse(response: String): Pair<String, String> {
        val root = basicJson.decodeFromString<JsonObject>(response)
        return root.getObject("Response").getObject("displayProperties").run {
            get("icon")!!.jsonPrimitive.content to get("name")!!.jsonPrimitive.content
        }
    }
}

suspend fun main() {
    println(Destiny2Translator().getModHashesFromResponse(sampleReference))
    println(Destiny2Translator().getNameAndImageForModResponse(sampleModReference))
}

const val sampleReference =
    """{"Response":{"vendor":{"privacy":2},"categories":{"privacy":2},"sales":{"data":{"1":{"saleStatus":0,"failureIndexes":[],"augments":0,"vendorItemIndex":1,"itemHash":1455474223,"quantity":1,"costs":[{"itemHash":3159615086,"quantity":10000,"hasConditionalVisibility":false}],"apiPurchasable":true},"2":{"saleStatus":0,"failureIndexes":[],"augments":0,"vendorItemIndex":2,"itemHash":171866827,"quantity":1,"costs":[{"itemHash":3159615086,"quantity":10000,"hasConditionalVisibility":false}],"apiPurchasable":true},"3":{"saleStatus":0,"failureIndexes":[],"augments":0,"vendorItemIndex":3,"itemHash":2259349108,"quantity":1,"costs":[{"itemHash":3159615086,"quantity":10000,"hasConditionalVisibility":false}],"apiPurchasable":true},"4":{"saleStatus":8,"failureIndexes":[0],"augments":0,"vendorItemIndex":4,"itemHash":4187422269,"quantity":1,"costs":[{"itemHash":3159615086,"quantity":10000,"hasConditionalVisibility":false}],"apiPurchasable":true},"5":{"saleStatus":8,"failureIndexes":[0],"augments":0,"vendorItemIndex":5,"itemHash":3675595381,"quantity":1,"costs":[{"itemHash":3159615086,"quantity":10000,"hasConditionalVisibility":false}],"apiPurchasable":true},"123":{"saleStatus":8,"failureIndexes":[5],"augments":0,"vendorItemIndex":123,"itemHash":1807758212,"quantity":1,"costs":[{"itemHash":4046539562,"quantity":10,"hasConditionalVisibility":false}],"overrideNextRefreshDate":"2021-12-12T17:00:00Z"},"130":{"saleStatus":8,"failureIndexes":[5],"augments":0,"vendorItemIndex":130,"itemHash":1484685886,"quantity":1,"costs":[{"itemHash":4046539562,"quantity":10,"hasConditionalVisibility":false}],"overrideNextRefreshDate":"2021-12-12T17:00:00Z"},"225":{"saleStatus":0,"failureIndexes":[],"augments":0,"vendorItemIndex":225,"itemHash":1365254141,"quantity":1,"costs":[{"itemHash":1022552290,"quantity":25,"hasConditionalVisibility":false},{"itemHash":3159615086,"quantity":1000,"hasConditionalVisibility":false}]},"226":{"saleStatus":0,"failureIndexes":[],"augments":0,"vendorItemIndex":226,"itemHash":1876989571,"quantity":1,"costs":[{"itemHash":1022552290,"quantity":25,"hasConditionalVisibility":false},{"itemHash":3159615086,"quantity":1000,"hasConditionalVisibility":false}]},"227":{"saleStatus":0,"failureIndexes":[],"augments":0,"vendorItemIndex":227,"itemHash":775812602,"quantity":1,"costs":[{"itemHash":1022552290,"quantity":25,"hasConditionalVisibility":false},{"itemHash":3159615086,"quantity":1000,"hasConditionalVisibility":false}]},"228":{"saleStatus":0,"failureIndexes":[],"augments":0,"vendorItemIndex":228,"itemHash":1800940324,"quantity":1,"costs":[{"itemHash":1022552290,"quantity":25,"hasConditionalVisibility":false},{"itemHash":3159615086,"quantity":1000,"hasConditionalVisibility":false}]},"229":{"saleStatus":0,"failureIndexes":[],"augments":0,"vendorItemIndex":229,"itemHash":3689406239,"quantity":1,"costs":[{"itemHash":1022552290,"quantity":25,"hasConditionalVisibility":false},{"itemHash":3159615086,"quantity":1000,"hasConditionalVisibility":false}]},"323":{"saleStatus":0,"failureIndexes":[],"augments":0,"vendorItemIndex":323,"itemHash":2979281381,"quantity":1,"costs":[{"itemHash":3853748946,"quantity":1,"hasConditionalVisibility":false},{"itemHash":1022552290,"quantity":10,"hasConditionalVisibility":false},{"itemHash":592227263,"quantity":25,"hasConditionalVisibility":false},{"itemHash":3159615086,"quantity":5000,"hasConditionalVisibility":false}],"overrideNextRefreshDate":"2021-12-12T17:00:00Z"},"324":{"saleStatus":0,"failureIndexes":[],"augments":0,"vendorItemIndex":324,"itemHash":2979281381,"quantity":1,"costs":[{"itemHash":3853748946,"quantity":1,"hasConditionalVisibility":false},{"itemHash":1022552290,"quantity":10,"hasConditionalVisibility":false},{"itemHash":3592324052,"quantity":25,"hasConditionalVisibility":false},{"itemHash":3159615086,"quantity":5000,"hasConditionalVisibility":false}],"overrideNextRefreshDate":"2021-12-12T17:00:00Z"},"327":{"saleStatus":0,"failureIndexes":[],"augments":0,"vendorItemIndex":327,"itemHash":4257549984,"quantity":1,"costs":[{"itemHash":3853748946,"quantity":10,"hasConditionalVisibility":false},{"itemHash":950899352,"quantity":25,"hasConditionalVisibility":false},{"itemHash":3159615086,"quantity":10000,"hasConditionalVisibility":false}],"overrideNextRefreshDate":"2021-12-12T17:00:00Z"},"333":{"saleStatus":0,"failureIndexes":[],"augments":0,"vendorItemIndex":333,"itemHash":4257549984,"quantity":1,"costs":[{"itemHash":3853748946,"quantity":10,"hasConditionalVisibility":false},{"itemHash":293622383,"quantity":25,"hasConditionalVisibility":false},{"itemHash":3159615086,"quantity":10000,"hasConditionalVisibility":false}],"overrideNextRefreshDate":"2021-12-12T17:00:00Z"},"339":{"saleStatus":2,"failureIndexes":[],"augments":0,"vendorItemIndex":339,"itemHash":4257549985,"quantity":1,"costs":[{"itemHash":4257549984,"quantity":10,"hasConditionalVisibility":false},{"itemHash":1485756901,"quantity":100,"hasConditionalVisibility":false},{"itemHash":3159615086,"quantity":50000,"hasConditionalVisibility":false}],"overrideNextRefreshDate":"2021-12-12T17:00:00Z"}},"privacy":2},"currencyLookups":{"privacy":2},"stringVariables":{"privacy":2}},"ErrorCode":1,"ThrottleSeconds":0,"ErrorStatus":"Success","Message":"Ok","MessageData":{}}"""

const val sampleModReference =
    """{
    "Response": {
        "displayProperties": {
            "description": "",
            "name": "Heavy Handed",
            "icon": "/common/destiny2_content/icons/c6d1db58becc093aa5d7b2534af41005.png",
            "iconSequences": [{
                "frames": ["/common/destiny2_content/icons/c6d1db58becc093aa5d7b2534af41005.png"]
            }, {
                "frames": ["/common/destiny2_content/icons/f74f76fabcf919795131996c2090bc87.png"]
            }],
            "hasIcon": true
        },
        "tooltipNotifications": [{
            "displayString": "This mod's secondary perk is active when at least one other Arc mod is socketed into this armor, or when at least one other Arc Charged With Light mod is socketed into another piece of armor you are wearing.",
            "displayStyle": "ui_display_style_perk_info"
        }],
        "collectibleHash": 1427972558,
        "backgroundColor": {
            "red": 0,
            "green": 0,
            "blue": 0,
            "alpha": 0
        },
        "itemTypeDisplayName": "Charged with Light Mod",
        "flavorText": "",
        "uiItemDisplayStyle": "ui_display_style_energy_mod",
        "itemTypeAndTierDisplayName": "Common Charged with Light Mod",
        "displaySource": "",
        "tooltipStyle": "build",
        "action": {
            "verbName": "Dismantle",
            "verbDescription": "",
            "isPositive": false,
            "requiredCooldownSeconds": 0,
            "requiredItems": [],
            "progressionRewards": [],
            "actionTypeLabel": "shard",
            "rewardSheetHash": 0,
            "rewardItemHash": 0,
            "rewardSiteHash": 0,
            "requiredCooldownHash": 0,
            "deleteOnAction": true,
            "consumeEntireStack": false,
            "useOnAcquire": false
        },
        "inventory": {
            "maxStackSize": 1,
            "bucketTypeHash": 2422292810,
            "recoveryBucketTypeHash": 0,
            "tierTypeHash": 3340296461,
            "isInstanceItem": false,
            "nonTransferrableOriginal": false,
            "tierTypeName": "Common",
            "tierType": 2,
            "expirationTooltip": "",
            "expiredInActivityMessage": "",
            "expiredInOrbitMessage": "",
            "suppressExpirationWhenObjectivesComplete": true
        },
        "plug": {
            "insertionRules": [],
            "plugCategoryIdentifier": "enhancements.season_v470",
            "plugCategoryHash": 208760563,
            "onActionRecreateSelf": false,
            "actionRewardSiteHash": 0,
            "actionRewardItemOverrideHash": 0,
            "insertionMaterialRequirementHash": 0,
            "previewItemOverrideHash": 0,
            "enabledMaterialRequirementHash": 0,
            "enabledRules": [{
                "failureMessage": ""
            }],
            "uiPlugLabel": "",
            "plugStyle": 0,
            "plugAvailability": 0,
            "alternateUiPlugLabel": "",
            "alternatePlugStyle": 0,
            "isDummyPlug": false,
            "energyCost": {
                "energyCost": 7,
                "energyTypeHash": 728351493,
                "energyType": 1
            },
            "applyStatsToSocketOwnerItem": false
        },
        "acquireRewardSiteHash": 0,
        "acquireUnlockHash": 0,
        "investmentStats": [{
            "statTypeHash": 3779394102,
            "value": 7,
            "isConditionallyActive": false
        }],
        "perks": [{
            "requirementDisplayString": "",
            "perkHash": 2155608219,
            "perkVisibility": 1
        }, {
            "requirementDisplayString": "",
            "perkHash": 132905028,
            "perkVisibility": 1
        }],
        "allowActions": true,
        "doesPostmasterPullHaveSideEffects": false,
        "nonTransferrable": true,
        "itemCategoryHashes": [59, 4104513227],
        "specialItemType": 0,
        "itemType": 19,
        "itemSubType": 0,
        "classType": 3,
        "breakerType": 0,
        "equippable": false,
        "defaultDamageType": 0,
        "isWrapper": false,
        "hash": 1484685886,
        "index": 11344,
        "redacted": false,
        "blacklisted": false
    },
    "ErrorCode": 1,
    "ThrottleSeconds": 0,
    "ErrorStatus": "Success",
    "Message": "Ok",
    "MessageData": {}
}"""