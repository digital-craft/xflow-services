#!/bin/bash
set -uo pipefail  # note : pas de `set -e` ici, incompatible avec la logique de timeout de `read`

WATCH_DIR="${1:-src}"
DEBOUNCE_SECONDS="${2:-0.3}"

echo "--- Starting inotifywait compile watcher on $WATCH_DIR (debounce: ${DEBOUNCE_SECONDS}s) ---"

if ! command -v inotifywait &> /dev/null; then
    echo "ERROR: inotifywait not found. Install inotify-tools."
    exit 1
fi

touch /tmp/.compile-stamp

resource_changed=0
java_changed=0

inotifywait -m -r -e modify,create,delete --format '%w%f' "$WATCH_DIR" 2>/dev/null | {
    while true; do
        read -r -t "$DEBOUNCE_SECONDS" FILE
        rc=$?

        if [ "$rc" -gt 128 ]; then
            # Timeout de `read` (pas d'evenement depuis DEBOUNCE_SECONDS) : on agit si besoin
            if [ "$java_changed" = "1" ]; then
                echo "--- Source Java changee, compilation ---"
                ./mvnw compile -q && touch /tmp/.compile-stamp
            elif [ "$resource_changed" = "1" ]; then
                echo "--- Ressource changee, process-resources ---"
                ./mvnw process-resources -q && touch /tmp/.compile-stamp
            fi
            resource_changed=0
            java_changed=0
            continue
        elif [ "$rc" -ne 0 ]; then
            # EOF reel du flux inotifywait (ex. process tue) : on sort proprement au lieu de boucler
            echo "--- inotifywait s'est arrete, fin du watcher ---"
            break
        fi

        case "$FILE" in
            */target/*|*/.git/*) continue ;;
        esac

        case "$FILE" in
            src/main/resources/*) resource_changed=1 ;;
            src/main/java/*)      java_changed=1 ;;
        esac
    done
}