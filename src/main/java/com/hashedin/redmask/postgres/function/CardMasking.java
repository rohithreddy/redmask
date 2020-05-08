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
 * This masking function is used to mask column containing credit/debit card data entered as string.
 * <p>
 * This function have the following variations :-
 * <p>
 * CREDIT_CARD_SHOW_FIRST : This show the first few character equal to number passed in the
 * show_first parameter.
 * </p>
 * <p>
 * CREDIT_CARD_SHOW_LAST: This show the last few character equal to number passed in the
 * show_last parameter.
 * </p>
 * <p>
 * CREDIT_CARD_SHOW_FIRST_LAST: This show the first few character  and the last few characters
 * equal to number passed in the show_first and show_last parameter respectively.
 * </p>
 * </p>
 */
public class CardMasking extends MaskingRuleDef {

  private static final Logger log = LoggerFactory.getLogger(CardMasking.class);

  private static final String MASK_TYPE_SHOW_FIRST = "'first'";
  private static final String MASK_TYPE_SHOW_LAST = "'last'";
  private static final String MASK_TYPE_SHOW_FIRST_LAST = "'firstnlast'";

  private static final String PARAM_SEPARATOR = "separator";
  private static final String PARAM_SEPARATOR_DEFAULT = "";

  private static final String PARAM_SHOW_FIRST = "show_first";
  private static final String PARAM_SHOW_FIRST_DEFAULT = "0";

  private static final String PARAM_SHOW_LAST = "show_last";
  private static final String PARAM_SHOW_LAST_DEFAULT = "0";

  private static final List<String> EXPECTED_PARAMETERS_LIST = new ArrayList<String>(
      Arrays.asList(PARAM_SEPARATOR, PARAM_SHOW_FIRST, PARAM_SHOW_LAST));

  public CardMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public CardMasking() {
  }

  /**
   * The function add the masking function definition to the be created to the funcSet.
   *
   * @param config  TemplateConfiguration object to be used to create the function definition.
   * @param funcSet Set of function to be created to run the intended mask view.
   */
  @Override
  public void addFunctionDefinition(TemplateConfiguration config, Set<String> funcSet) {
    try {
      funcSet.add(MaskingQueryUtil.maskNumbers(config));
      funcSet.add(MaskingQueryUtil.maskPaymentCard(config));
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
        return MaskingQueryUtil.processQueryTemplate(config,
            MaskingConstants.MASK_CARD_FUNC, paramsList);
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
      parameters.addAll(Arrays.asList(PARAM_SEPARATOR_DEFAULT, PARAM_SHOW_FIRST_DEFAULT,
          PARAM_SHOW_LAST_DEFAULT));
    }

    String separator = this.getMaskParams()
        .getOrDefault(PARAM_SEPARATOR, PARAM_SEPARATOR_DEFAULT);
    int val1 = Integer.parseInt(this.getMaskParams()
        .getOrDefault(PARAM_SHOW_FIRST, PARAM_SHOW_FIRST_DEFAULT));
    int val2 = Integer.parseInt(this.getMaskParams()
        .getOrDefault(PARAM_SHOW_LAST, PARAM_SHOW_LAST_DEFAULT));

    if (val1 < 0) {
      throw new RedmaskConfigException(
          String.format("\'%s\' value should be greater than or equal to 0", PARAM_SHOW_FIRST));
    }

    if (val2 < 0) {
      throw new RedmaskConfigException(
          String.format("\'%s\' value should be greater than or equal to 0", PARAM_SHOW_LAST));
    }

    switch (this.getMaskType()) {
      case CREDIT_CARD_SHOW_FIRST:
        parameters.add(MASK_TYPE_SHOW_FIRST);
        break;

      case CREDIT_CARD_SHOW_LAST:
        parameters.add(MASK_TYPE_SHOW_LAST);
        break;

      case CREDIT_CARD_SHOW_FIRST_LAST:
        parameters.add(MASK_TYPE_SHOW_FIRST_LAST);
        break;
      default:
        break;
    }
    parameters.add("'" + separator + "'");
    parameters.add(String.valueOf(val1));
    parameters.add(String.valueOf(val2));

    return true;
  }
}