#!/bin/bash
#
# Despliegue del Engine en produccion.
#
#   ./deploy.sh              compila (sin tests) y despliega
#   ./deploy.sh --tests      compila ejecutando los tests
#   ./deploy.sh --no-build   despliega el jar ya existente en target/
#   ./deploy.sh --rollback   restaura el ultimo backup
#   ./deploy.sh --list       lista los backups disponibles
#
# Los catalogos .def y application.properties de produccion NO se sobrescriben:
# el Engine los modifica en tiempo de ejecucion. Solo se copian si no existen.

set -euo pipefail

REPO_DIR="$(cd "$(dirname "$0")" && pwd)"
PROD_DIR="${PROD_DIR:-/home/tecnico/Proyecto/Distribucion/Engine}"
BACKUP_DIR="${BACKUP_DIR:-/home/tecnico/Proyecto/Distribucion/Engine_backups}"
KEEP_BACKUPS=10
JAR_NAME="Engine-0.0.1-SNAPSHOT.jar"
PORT=8090

# Ficheros que se actualizan siempre desde el repo
CSV_FILES="ATHS.csv IL.csv PC.csv SCO.csv"
# Ficheros de estado de produccion: solo se copian si faltan
KEEP_FILES="catalogoConsultas.def catalogoModelFilters.def application.properties"

info()  { echo -e "\e[1;34m==>\e[0m $*"; }
warn()  { echo -e "\e[1;33m[AVISO]\e[0m $*"; }
fail()  { echo -e "\e[1;31m[ERROR]\e[0m $*" >&2; exit 1; }

engine_running() {
	pgrep -f "java.*$JAR_NAME" >/dev/null || ss -ltn 2>/dev/null | grep -q ":$PORT "
}

check_stopped() {
	if engine_running; then
		fail "El Engine esta en ejecucion (proceso $JAR_NAME o puerto $PORT). Detenlo antes de desplegar."
	fi
}

backup() {
	local dest="$BACKUP_DIR/$(date +%Y%m%d_%H%M%S)"
	info "Backup de produccion en $dest"
	mkdir -p "$dest"
	for f in "$JAR_NAME" $CSV_FILES $KEEP_FILES DEPLOY_INFO; do
		[ -f "$PROD_DIR/$f" ] && cp -p "$PROD_DIR/$f" "$dest/"
	done
	# Rotacion: conservar solo los ultimos $KEEP_BACKUPS
	ls -1d "$BACKUP_DIR"/*/ 2>/dev/null | sort | head -n -"$KEEP_BACKUPS" | xargs -r rm -rf
}

rollback() {
	check_stopped
	local last
	last="$(ls -1d "$BACKUP_DIR"/*/ 2>/dev/null | sort | tail -n 1)"
	[ -n "$last" ] || fail "No hay backups en $BACKUP_DIR"
	info "Restaurando $last"
	[ -f "$last/DEPLOY_INFO" ] && cat "$last/DEPLOY_INFO"
	read -r -p "¿Restaurar jar, CSV y catalogos .def de este backup? [s/N] " ok
	[[ "$ok" =~ ^[sS]$ ]] || exit 0
	cp -p "$last"/* "$PROD_DIR/"
	info "Rollback completado"
}

NO_BUILD=0
SKIP_TESTS="-DskipTests"
case "${1:-}" in
	--rollback) rollback; exit 0 ;;
	--list)     ls -1 "$BACKUP_DIR" 2>/dev/null || echo "Sin backups"; exit 0 ;;
	--no-build) NO_BUILD=1 ;;
	--tests)    SKIP_TESTS="" ;;
	"")         ;;
	*)          sed -n '3,12p' "$0"; exit 1 ;;
esac

[ -d "$PROD_DIR" ] || fail "No existe la carpeta de produccion $PROD_DIR"
cd "$REPO_DIR"

BRANCH="$(git rev-parse --abbrev-ref HEAD)"
COMMIT="$(git rev-parse --short HEAD)"
if [ -n "$(git status --porcelain)" ]; then
	warn "Hay cambios sin commitear en $BRANCH; el jar no correspondera exactamente a $COMMIT."
	read -r -p "¿Continuar? [s/N] " ok
	[[ "$ok" =~ ^[sS]$ ]] || exit 1
	COMMIT="$COMMIT (con cambios locales)"
fi

check_stopped

if [ "$NO_BUILD" -eq 0 ]; then
	info "Compilando ($BRANCH @ $COMMIT)"
	./mvnw -q clean package $SKIP_TESTS || fail "Fallo la compilacion"
fi
[ -f "target/$JAR_NAME" ] || fail "No se encuentra target/$JAR_NAME"

backup

info "Copiando $JAR_NAME"
cp "target/$JAR_NAME" "$PROD_DIR/$JAR_NAME"

for f in $CSV_FILES; do
	if ! cmp -s "$f" "$PROD_DIR/$f"; then
		info "Actualizando $f"
		cp "$f" "$PROD_DIR/$f"
	fi
done

for f in $KEEP_FILES; do
	src="$f"
	[ "$f" = "application.properties" ] && src="src/main/resources/$f"
	if [ ! -f "$PROD_DIR/$f" ]; then
		info "No existia $f en produccion, se copia del repo"
		cp "$src" "$PROD_DIR/$f"
	elif ! cmp -s "$src" "$PROD_DIR/$f"; then
		warn "$f difiere del repo; se conserva el de produccion"
	fi
done

cat > "$PROD_DIR/DEPLOY_INFO" <<EOF
fecha:  $(date '+%Y-%m-%d %H:%M:%S')
rama:   $BRANCH
commit: $COMMIT
EOF

info "Despliegue completado ($BRANCH @ $COMMIT)"
echo "    Arranca el Engine con: /home/tecnico/Proyecto/Distribucion/BootEngine.sh"
