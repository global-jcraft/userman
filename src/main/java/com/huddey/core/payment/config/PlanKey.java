package com.huddey.core.payment.config;

import java.util.Locale;

import lombok.Getter;

@Getter
public enum PlanKey {
  FREE("huddey_free"),
  STARTER("huddey_starter"),
  PRO("huddey_pro"),
  ELITE("huddey_elite"),
  TEAMS("huddey_teams"),
  TEAMS_PRO("huddey_teams_pro");

  private final String key;

  PlanKey(String key) {
    this.key = key;
  }

  public String lookup(String interval, String currency) {
    return key + ":" + interval.toLowerCase(Locale.ROOT) + ":" + currency.toUpperCase(Locale.ROOT);
  }
}
