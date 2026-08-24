package org.gridmind.backend.locate.application

import org.gridmind.backend.locate.domain.LocateHighlight

/**
 * Announces which storage locations should currently be highlighted, and in what color.
 * [highlights] is always the *complete* desired state, never a delta — a subscriber (an
 * addressable LED controller, eventually) is expected to turn off anything not present in
 * the list, so publishing an empty list is how a search that no longer matches anything
 * clears every light.
 */
interface LocatePublisherPort {
    fun publish(highlights: List<LocateHighlight>)

    /** Whether the broker is currently reachable — used only for diagnostics (the admin
     * status dashboard), never on the search path itself. */
    fun isConnected(): Boolean
}
