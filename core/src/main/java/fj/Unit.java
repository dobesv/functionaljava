package fj;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The unit type which has only one value.
 *
 * @version %build.number%
 */
public final class Unit {
  private static final Unit u = new Unit();

  private Unit() {

  }

  /**
   * The only value of the unit type.
   *
   * @return The only value of the unit type.
   */
  @NonNull
  public static Unit unit() {
    return u;
  }
}
