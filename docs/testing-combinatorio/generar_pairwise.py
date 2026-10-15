from allpairspy import AllPairs
from pathlib import Path
import csv

parametros = {
    "Prevision": [
        "FONASA_A",
        "FONASA_BD",
        "ISAPRE",
        "PARTICULAR",
    ],

    "Especialidad": [
        "MEDICINA_GENERAL",
        "NUTRICION",
        "PEDIATRIA",
        "CARDIOLOGIA",
        "PSIQUIATRIA",
    ],

    "Canal": [
        "WEB",
        "APP_MOVIL",
        "CALL_CENTER",
        "PRESENCIAL",
    ],

    "Anticipacion": [
        "PASADO_O_AHORA",
        "MENOR_60_MIN",
        "60_MIN",
        "61_239_MIN",
        "240_MIN",
        "MAYOR_240_MIN_MENOR_60D",
        "60_DIAS",
        "MAYOR_60_DIAS",
    ],

    "HistorialInasistencias": [
        "SIN_INASISTENCIAS",
        "1_2_EN_6_MESES",
        "3_EN_6_MESES_BLOQUEO_VIGENTE",
        "3_EN_6_MESES_BLOQUEO_EXPIRADO",
    ],

    "Edad": [
        "MENOR_65",
        "65",
        "MAYOR_65",
    ],

    "Convenio": [
        "SIN_CONVENIO",
        "CON_CONVENIO",
    ],

    "CupoBloque": [
        "DISPONIBLE",
        "OCUPADO_SIN_ESPERA",
        "OCUPADO_CON_ESPERA",
    ],
}

nombres = list(parametros.keys())
valores = list(parametros.values())

casos = list(AllPairs(valores))

salida = Path("docs/testing-combinatorio/casos-pairwise.tsv")

with salida.open("w", newline="", encoding="utf-8") as archivo:
    writer = csv.writer(archivo, delimiter="\t")

    writer.writerow(nombres)

    for caso in casos:
        writer.writerow(caso)

print(f"Casos pairwise generados: {len(casos)}")
print(f"Archivo generado: {salida}")