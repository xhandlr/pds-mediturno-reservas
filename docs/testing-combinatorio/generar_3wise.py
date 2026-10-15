from itertools import product
from pathlib import Path
import csv

anticipacion = [
    "PASADO_O_AHORA",
    "MENOR_60_MIN",
    "60_MIN",
    "61_239_MIN",
    "240_MIN",
    "MAYOR_240_MIN_MENOR_60D",
    "60_DIAS",
    "MAYOR_60_DIAS",
]

historial = [
    "SIN_INASISTENCIAS",
    "1_2_EN_6_MESES",
    "3_EN_6_MESES_BLOQUEO_VIGENTE",
    "3_EN_6_MESES_BLOQUEO_EXPIRADO",
]

canal = [
    "WEB",
    "APP_MOVIL",
    "CALL_CENTER",
    "PRESENCIAL",
]

casos = list(product(anticipacion, historial, canal))

salida = Path("docs/testing-combinatorio/casos-3wise.tsv")

with salida.open("w", newline="", encoding="utf-8") as archivo:
    writer = csv.writer(archivo, delimiter="\t")

    writer.writerow([
        "Anticipacion",
        "HistorialInasistencias",
        "Canal",
    ])

    writer.writerows(casos)

print(f"Casos 3-wise generados: {len(casos)}")
print(f"Archivo generado: {salida}")
