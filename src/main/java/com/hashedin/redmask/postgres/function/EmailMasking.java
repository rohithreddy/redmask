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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This masking function is used to mask column containing email data entered as string.
 * <p>
 *   This function have the following variations :-
 *   <p>
 *     EMAIL_SHOW_DOMAIN : This shows only the domain part of the email address.
 *   </p>
 *   <p>
 *     EMAIL_SHOW_FIRST_CHARACTER_DOMAIN : This shows the first character and the domain part of the
 *     email address.
 *   </p>
 *   <p>
 *     EMAIL_SHOW_FIRST_CHARACTERS : This shows the first few character equal to number passed in
 *     the show_first parameter.
 *   </p>
 *   <p>
 *     EMAIL_MASK_ALPHANUMERIC : This mask all the alphanumeric character in the email address.
 *   </p>
 * </p>
 */
public class EmailMasking extends MaskingRuleDef {

  private static final Logger log = LoggerFactory.getLogger(EmailMasking.class);

  private static final String MASK_TYPE_SHOW_DOMAIN = "'domain'";
  private static final String MASK_TYPE_SHOW_FIRST_DOMAIN = "'firstndomain'";
  private static final String MASK_TYPE_SHOW_FIRST_CHAR = "'firstN'";
  private static final String MASK_TYPE_SHOW_SPECIAL_CHAR = "'nonspecialcharacter'";

  private static final String PARAM_SHOW_FIRST_CHARACTERS = "show_first";
  private static final String PARAM_SHOW_FIRST_CHARACTERS_DEFAULT = "0";


  public EmailMasking(
      String columnName,
      MaskType maskType,
      Map<String, String> maskParams) {
    super(columnName, maskType, maskParams);
  }

  public EmailMasking() {
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
      funcSet.add(MaskingQueryUtil.maskString(config));
      funcSet.add(MaskingQueryUtil.maskEmail(config));
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
            MaskingConstants.MASK_EMAIL_FUNC, paramsList);
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
   * @param parameters List of parameters required to create the intended mask.
   * @return The list of validated parameter
   * @throws RedmaskConfigException
   */
  private boolean validateAndAddParameters(List<String> parameters)
      throws RedmaskConfigException {
    for (String key : this.getMaskParams().keySet()) {
      if (!key.equals(PARAM_SHOW_FIRST_CHARACTERS)) {
        throw new RedmaskConfigException("Unrecognised parameter" + key + " supplied to "
            + this.getMaskType() + " for column " + this.getColumnName());
      }
    }
    switch (this.getMaskType()) {
      case EMAIL_SHOW_DOMAIN:
        parameters.add(MASK_TYPE_SHOW_DOMAIN);
        break;

      case EMAIL_SHOW_FIRST_CHARACTER_DOMAIN:
        parameters.add(MASK_TYPE_SHOW_FIRST_DOMAIN);
        break;

      case EMAIL_SHOW_FIRST_CHARACTERS:
        parameters.add(MASK_TYPE_SHOW_FIRST_CHAR);

        if (this.getMaskParams().isEmpty() || this.getMaskParams() == null) {
          parameters.add(PARAM_SHOW_FIRST_CHARACTERS_DEFAULT);
        }
        int showCharacters = Integer.parseInt(this.getMaskParams()
            .getOrDefault(PARAM_SHOW_FIRST_CHARACTERS, PARAM_SHOW_FIRST_CHARACTERS_DEFAULT));
        if (showCharacters > 0) {
          parameters.add(String.valueOf(showCharacters));
        } else {
          throw new RedmaskConfigException(
              String.format("\'%s\' value should be greater than 0",
                  PARAM_SHOW_FIRST_CHARACTERS));
        }
        break;
      case EMAIL_MASK_ALPHANUMERIC:
        parameters.add(MASK_TYPE_SHOW_SPECIAL_CHAR);
        break;
      default:
        return false;
    }
    return true;
  }
}
