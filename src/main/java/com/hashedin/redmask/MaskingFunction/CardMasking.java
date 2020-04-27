package com.hashedin.redmask.MaskingFunction;

import com.hashedin.redmask.configurations.MaskType;
import com.hashedin.redmask.configurations.MaskingConstants;
import com.hashedin.redmask.configurations.TemplateConfiguration;
import com.hashedin.redmask.exception.InvalidParameterValueException;
import com.hashedin.redmask.exception.UnknownParameterException;
import com.hashedin.redmask.service.MaskingQueryUtil;
import com.hashedin.redmask.service.MaskingRuleDef;
import freemarker.template.TemplateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  @Override
  public void addFunctionDefinition(TemplateConfiguration config, Set<String> funcSet) {
    try {
      funcSet.add(MaskingQueryUtil.maskNumbers(config));
      funcSet.add(MaskingQueryUtil.maskPaymentCard(config));
      log.info("Function added for Mask Type {}", this.getMaskType());
    } catch (IOException | TemplateException ex) {
      log.error("Error occurred while adding MaskFunction for Mask Type {} ", this.getMaskType());
    }
  }


  @Override
  public String getSubQuery(TemplateConfiguration config, String tableName)
      throws InvalidParameterValueException, UnknownParameterException {
    List<String> paramsList = new ArrayList<>();
    paramsList.add(this.getColumnName());
    try {
      if (validateAndAddParameters(paramsList)) {
        return MaskingQueryUtil.processQueryTemplate(config,
            MaskingConstants.MASK_CARD_FUNC, paramsList);
      }
    } catch (IOException | TemplateException ex) {
      log.error("Error occurred while adding MaskFunction for Mask Type {} ", this.getMaskType());
    }
    return this.getColumnName();

  }

  private boolean validateAndAddParameters(List<String> parameters)
      throws InvalidParameterValueException, UnknownParameterException {

    for (String key : this.getMaskParams().keySet()) {
      if (!EXPECTED_PARAMETERS_LIST.contains(key)) {
        throw new UnknownParameterException("Unrecognised parameter" + key + " supplied to "
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
      throw new InvalidParameterValueException(
          String.format("\'%s\' value should be greater than or equal to 0", PARAM_SHOW_FIRST));
    }

    if (val2 < 0) {
      throw new InvalidParameterValueException(
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
    parameters.add(separator);
    parameters.add(String.valueOf(val1));
    parameters.add(String.valueOf(val2));

    return true;
  }
}