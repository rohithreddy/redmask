package com.hashedin.redmask.postgres.function;

import com.hashedin.redmask.common.MaskingQueryUtil;
import com.hashedin.redmask.common.MaskingRuleDef;
import com.hashedin.redmask.config.MaskType;
import com.hashedin.redmask.config.MaskingConstants;
import com.hashedin.redmask.config.TemplateConfiguration;
import com.hashedin.redmask.exception.RedmaskConfigException;
import com.hashedin.redmask.exception.RedmaskRuntimeException;

import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This masking function allows you to mask a string type column with a user defined pattern
 * specified as the pattern parameter. The user can also leave the first and last few character of
 * the string as unmasked by specifying the number of unmasked character in show_first and
 * show_last parameter respectively.
 */
public class StringMasking extends MaskingRuleDef {

  private static final Logger log = LoggerFactory.getLogger(StringMasking.class);

  private static final String PARAM_REPLACEMENT_PATTERN = "pattern";
  private static final String PARAM_PATTERN_DEFAULT = "*";

  private static final String PARAM_SHOW_FIRST = "show_first";
  private static final String PARAM_SHOW_FIRST_DEFAULT = "0";

  private static final String PARAM_SHOW_LAST = "show_last";
  private static final String PARAM_SHOW_LAST_DEFAULT = "0";

  private static final List<String> EXPECTED_PARAMETERS_LIST = new ArrayList<String>(
      Arrays.asList(PARAM_REPLACEMENT_PATTERN, PARAM_SHOW_FIRST, PARAM_SHOW_LAST));

  public StringMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public StringMasking() {
  }

  /**
   * The function add the masking function definition to the be created to the funcSet.
   *  @param config  TemplateConfiguration object to be used to create the function definition.
   * @param funcSet Set of function to be created to run the intended mask view.
   * @param dbType
   */
  @Override
  public void addFunctionDefinition(TemplateConfiguration config, Set<String> funcSet,
                                    String dbType) {
    try {
      funcSet.add(MaskingQueryUtil.maskString(config, dbType));
      log.info("Function added for Mask Type {}", this.getMaskType());
    } catch (IOException | TemplateException ex) {
      throw new RedmaskRuntimeException(String.format("Error occurred while adding MaskFunction"
          + " for Mask Type %s ", this.getMaskType()), ex);
    }
  }

  /**
   * This function is used to generate the SQL subquery that applies the intended mask onto
   * the column and add an alias as the original column name
   *
   * @param config    Template configuration in order to access the template used to create the
   *                  subquery.
   * @param tableName The name of the table.
   * @return The SubQuery designed specifically as per the mask and the masking parameters
   * provided by the user.
   * @throws RedmaskConfigException
   */
  @Override
  public String getSubQuery(TemplateConfiguration config, String tableName)
      throws RedmaskConfigException {
    List<String> paramsList = new ArrayList<>();
    paramsList.add(this.getColumnName());
    try {
      if (validateAndAddParameters(paramsList)) {
        return MaskingQueryUtil.processQueryTemplate(
            config, MaskingConstants.MASK_STRING_FUNC, paramsList);
      }
    } catch (IOException | TemplateException ex) {
      throw new RedmaskRuntimeException(String.format("Error occurred while making SQL Sub query"
              + "for column  %s  in table %s for Mask Type %s ", this.getColumnName(),
          tableName, this.getMaskType()), ex);
    }
    return this.getColumnName();
  }

  /**
   * <p>
   * This function validates whether the correct parameter have been supplied by
   * the user in the configuration file. It also check whether each parameter has a valid value and
   * then adds these parameter in their respective order into the parameter list.
   * </p>
   * <p>
   * The Function will add the default value of the parameters value is not passed in the
   * maskparams config.
   * </p>
   *
   * @param parameters List of parameters required to create the intended mask.
   * @return The list of validated parameter
   * @throws RedmaskConfigException
   */
  private boolean validateAndAddParameters(List<String> parameters)
      throws RedmaskConfigException {
    for (String key : this.getMaskParams().keySet()) {
      if (!EXPECTED_PARAMETERS_LIST.contains(key)) {
        throw new RedmaskConfigException("Unrecognised parameter" + key + " supplied to "
            + this.getMaskType() + " for column " + this.getColumnName());
      }
    }

    if (this.getMaskParams().isEmpty() || this.getMaskParams() == null) {
      parameters.addAll(Arrays.asList(PARAM_PATTERN_DEFAULT,
          PARAM_SHOW_FIRST_DEFAULT, PARAM_SHOW_LAST_DEFAULT));
    }
    String pattern = this.getMaskParams()
        .getOrDefault(PARAM_REPLACEMENT_PATTERN, PARAM_PATTERN_DEFAULT);
    int prefix = Integer.parseInt(this.getMaskParams()
        .getOrDefault(PARAM_SHOW_FIRST, PARAM_SHOW_FIRST_DEFAULT));
    int suffix = Integer.parseInt(this.getMaskParams()
        .getOrDefault(PARAM_SHOW_LAST, PARAM_SHOW_LAST_DEFAULT));

    if (prefix < 0) {
      throw new RedmaskConfigException(
          String.format("\'%s\' value should be greater than or equal to 0", PARAM_SHOW_FIRST));

    }
    if (suffix < 0) {
      throw new RedmaskConfigException(
          String.format("\'%s\' value should be greater than or equal to 0", PARAM_SHOW_LAST));
    }
    parameters.add("'" + pattern + "'");
    parameters.add(String.valueOf(prefix));
    parameters.add(String.valueOf(suffix));
    return true;
  }
}