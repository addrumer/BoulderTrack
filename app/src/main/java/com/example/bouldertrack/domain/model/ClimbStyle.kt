package com.example.bouldertrack.domain.model

/**
 * Style przejść boulderingowych zgodne ze standardową nomenklaturą wspinaczkową.
 */
enum class ClimbStyle {
    /** Problem pokonany czysto za pierwszym podejściem, bez wcześniejszego patentowania. */
    FLASH,

    /** Problem ukończony sukcesem po dwóch lub więcej próbach. */
    TOP,

    /** Nieukończony problem, który jest obecnie rozpracowywany (w trakcie prób). */
    PROJECT
}