package com.kyledahlin.raidrule


val BigBoyRaids = listOf(
    Raid(
        name = "Last Wish",
        id = "lastwish",
        encounters = listOf(
            Encounter("Kalli", "kalli", listOf()),
            Encounter("Shuro Chi", "shuro", listOf()),
            Encounter("Morgeth", "morgeth", listOf()),
            Encounter("Vault", "vault", listOf()),
            Encounter("Riven", "riven", listOf()),
            Encounter("Queenswalk", "queenswalk", listOf())
        )
    ),

    Raid(
        name = "Kings Fall",
        id = "kingsfall",
        encounters = listOf(
            Encounter(
                "Totems/Door", "totems", listOf(
                    EncounterRole("Left", size = 2, isRequired = true),
                    EncounterRole("Right", size = 2, isRequired = true)
                )
            ),
            Encounter(
                "Warpriest", "warpriest", listOf(
                    EncounterRole(name = "Middle", size = 2, isRequired = true),
                    EncounterRole(name = "Left", size = 2, isRequired = true),
                    EncounterRole(name = "Right", size = 2, isRequired = true)
                )
            ),
            Encounter(
                "Golgy", "golgy", listOf(
                    EncounterRole(name = "Gaze", size = 2, isRequired = true),
                    EncounterRole(name = "DPS", size = 4, isRequired = false)
                )
            ),
            Encounter(
                "Daughters", "daughters", listOf(
                    EncounterRole(name = "Plate", size = 4, isRequired = true),
                    EncounterRole(name = "Add clear", size = 2, isRequired = false)
                )
            ),
            Encounter(
                "Oryx", "oryx", listOf(
                    EncounterRole(name = "Plate", size = 4, isRequired = true),
                    EncounterRole(name = "Add clear", size = 2, isRequired = false)
                )
            ),
        )
    ),
    Raid(
        name = "Vow of the Disciple",
        id = "vow",
        encounters = listOf(
            Encounter(
                name = "Acquisition", "acquisition", listOf(
                    EncounterRole("Obelisk", size = 3, isRequired = true),
                    EncounterRole("Runner", size = 3, isRequired = false),
                )
            ),
            Encounter(
                name = "Caretaker", "caretaker", listOf(
                    EncounterRole("Symbols", size = 3, isRequired = true),
                    EncounterRole("Stunning", size = 2, isRequired = true),
                    EncounterRole("Add clear", size = 3, isRequired = false)
                )
            ),
            Encounter(
                name = "Exhibition", "exhibition", listOf(
                    EncounterRole("Nut", size = 1, isRequired = true),
                    EncounterRole("Shield", size = 1, isRequired = true),
                    EncounterRole("Flex", size = 5, isRequired = false)
                )
            ),
            Encounter(
                name = "Rhulk", "rhulk", listOf(
                    EncounterRole("Madoff", size = 4, isRequired = true),
                    EncounterRole("Add clear", size = 3, isRequired = false)
                )
            )
        )
    ),
)

val SmallBoyRaids = listOf(
    Raid(
        name = "Vault of Glass",
        id = "vog",
        encounters = listOf(
            Encounter(
                name = "Door",
                id = "door",
                listOf(
                    EncounterRole("Left", isRequired = true, size = 2),
                    EncounterRole("Right", isRequired = true, size = 2),
                    EncounterRole("Middle", isRequired = true, size = 2),
                )
            ),
            Encounter(
                name = "Conflux",
                id = "conflux",
                listOf(
                    EncounterRole("Left", isRequired = true, size = 2),
                    EncounterRole("Right", isRequired = true, size = 2),
                    EncounterRole("Middle", isRequired = true, size = 2),
                )
            ),
            Encounter(
                name = "Oracles",
                id = "oracles",
                listOf(
                    EncounterRole("Left", isRequired = true, size = 2),
                    EncounterRole("Right", isRequired = true, size = 2),
                    EncounterRole("Middle", isRequired = true, size = 2),
                )
            ),
            Encounter(
                name = "Templar",
                id = "templar",
                listOf(
                    EncounterRole("Relic", isRequired = true, size = 1),
                    EncounterRole("DPS", isRequired = false, size = 5)
                )
            ),
            Encounter(
                name = "Gatekeeper",
                id = "gatekeeper",
                listOf(
                    EncounterRole("Left start", isRequired = true, size = 3),
                    EncounterRole("Right start", isRequired = true, size = 3)
                )
            ),
            Encounter(
                name = "Atheon",
                id = "atheon",
                listOf(
                    EncounterRole("Circus mode", isRequired = true, size = 6)
                )
            ),
        )
    ),
    Raid(
        name = "Deep Stone Crypt",
        id = "dsc",
        encounters = listOf(
            Encounter(
                "Crypt", "crypt", listOf(
                    EncounterRole("Runner", size = 1, isRequired = true),
                    EncounterRole("Light", size = 3, isRequired = true),
                    EncounterRole("Dark", size = 3, isRequired = true)
                )
            ),
            Encounter(
                "Atraks", "atraks", listOf(
                    EncounterRole("Burster", size = 6, isRequired = true)
                )
            ),
            Encounter(
                "Descent", "descent", listOf(
                    EncounterRole("Flex", size = 6, isRequired = true)
                )
            ),
            Encounter(
                "Taniks", "taniks", listOf(
                    EncounterRole("Suppressor", size = 2, isRequired = true),
                    EncounterRole("Scanner", size = 2, isRequired = true),
                    EncounterRole("Operator", size = 2, isRequired = true)
                )
            ),
        )
    ),
    Raid(
        name = "Root of Nightmares",
        id = "root",
        encounters = listOf(
            Encounter(
                name = "Seed",
                id = "seed",
                listOf(
                    EncounterRole("Runner", isRequired = true, size = 1),
                    EncounterRole("Psions", isRequired = false, size = 1)
                )
            ),
            Encounter(
                name = "Floors",
                id = "floors",
                listOf(
                    EncounterRole("Runner", isRequired = true, size = 2),
                    EncounterRole("Defenders", isRequired = false, size = 4)
                )
            ),
            Encounter(
                name = "Planets",
                id = "planets",
                listOf(
                    EncounterRole("Bottom planets", isRequired = true, size = 2),
                    EncounterRole("Top planets", isRequired = true, size = 2),
                    EncounterRole("Add clear", isRequired = false, size = 2)
                )
            ),
            Encounter(
                name = "Nezcafe",
                id = "nezarec",
                listOf(
                    EncounterRole("Runner", isRequired = true, size = 2),
                    EncounterRole("Add clear", isRequired = false, size = 5)
                )
            ),
        )
    ),
)
