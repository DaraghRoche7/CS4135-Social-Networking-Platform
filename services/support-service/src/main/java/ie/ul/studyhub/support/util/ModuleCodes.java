package ie.ul.studyhub.support.util;

public final class ModuleCodes {
  private ModuleCodes() {}

  /** e.g. {@code 4135} → {@code CS4135}; otherwise upper-case trim. */
  public static String normalize(String raw) {
    if (raw == null) {
      return "";
    }
    String s = raw.trim().toUpperCase();
    if (s.matches("[0-9]{4}")) {
      return "CS" + s;
    }
    return s;
  }
}
