package uk.m0nom.adifweb.domain;

import lombok.Getter;

@Getter
public enum HtmlParameterType {
    FILENAME("filename"),
    LOCATION("location"),
    SOTA_REF("sotaRef"),
    WOTA_REF("wotaRef"),
    HEMA_REF("hemaRef"),
    POTA_REF("potaRef"),
    WWFF_REF("wwffRef"),
    COTA_REF("cotaRef"),
    LOTA_REF("lotaRef"),
    ROTA_REF("rotaRef"),
    IOTA_REF("iotaRef"),
    ENCODING("encoding"),
    STATION_SUBLABEL("stationSubLabel"),
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
    OPTIONS_VISIBLE("optionsVisible");

    private String parameterName;

    HtmlParameterType(String parameterName) {
        this.parameterName = parameterName;
    }
}
