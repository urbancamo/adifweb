package uk.m0nom.adifweb.domain;

import lombok.Getter;

@Getter
public enum HtmlParameterType {
    CALLSIGN("callsign"),
    FILENAME("filename"),
    LOCATION("location"),
    SOTA_REF("sotaRef"),
    GMA_REF("gmaRef"),
    WOTA_REF("wotaRef"),
    HEMA_REF("hemaRef"),
    POTA_REF("potaRef"),
    WWFF_REF("wwffRef"),
    COTA_REF("cotaRef"),
    LOTA_REF("lotaRef"),
    ROTA_REF("rotaRef"),
    IOTA_REF("iotaRef"),
    BOTA_REF("botaRef"),
    ENCODING("encoding"),
    STATION_SUBLABEL("stationSubLabel"),
    ACTIVITY_SUBLABEL("activitySubLabel"),
    LOCAL_ACTIVATION_SITES("localActivationSites"),
    LOCAL_ACTIVATION_SITES_RADIUS("localActivationSitesRadius"),
    ANTENNA("antenna"),
    CONTEST_RESULTS("contestResults"),
    SATELLITE_NAME("satName"),
    SATELLITE_MODE("satMode"),
    SATELLITE_BAND("satBand"),
    SOTA_MICROWAVE_AWARD_COMMENT("sotaMicrowaveAwardComment"),
    STRIP_COMMENT("stripComment"),
    PRINTER_CONFIG("printerConfig"),
    OPTIONS_VISIBLE("optionsVisible"),
    DONT_QSL_CALLSIGNS("dontQsl"),
    QSL_LABELS_INITIAL_POSITION("qslLabelsInitialPosition"),
    COMPACT_QSO_TEMPLATE("compactQsoTemplate"),
    PORTABLE_ICON("portableIcon"),
    COLOUR_CONTACTS_BASED_ON_BAND("colourContactsBasedOnBand");

    private final String parameterName;

    HtmlParameterType(String parameterName) {
        this.parameterName = parameterName;
    }
}
