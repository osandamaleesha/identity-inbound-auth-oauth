/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.oauth2.dcr.endpoint.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.oauth.dcr.exception.DCRMException;
import org.wso2.carbon.identity.oauth.dcr.service.DCRMService;
import org.wso2.carbon.identity.oauth2.dcr.endpoint.dto.RegistrationRequestDTO;
import org.wso2.carbon.identity.oauth2.dcr.endpoint.dto.UpdateRequestDTO;
import org.wso2.carbon.identity.oauth2.dcr.endpoint.exceptions.DCRMEndpointException;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@WithCarbonHome
@Listeners(MockitoTestNGListener.class)
public class DCRMUtilsTest {

    private List<String> redirectUris = new ArrayList<>();
    private List<String> grantTypes = new ArrayList<>();
    private final String clientName = "Application";

    @Mock
    PrivilegedCarbonContext privilegedCarbonContext;

    private MockedStatic<PrivilegedCarbonContext> mockedPrivilegedCarbonContext;

    @BeforeMethod
    public void setUp() throws Exception {

        mockedPrivilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
        when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        lenient().when(PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(DCRMService.class, null))
                .thenReturn(mock(DCRMService.class));

        redirectUris.add("https://op.certification.openid.net:60845/authz_cb");
        grantTypes.add("authorization_code");
    }

    @AfterMethod
    public void tearDown() {

        mockedPrivilegedCarbonContext.close();
    }

    @Test
    public void testGetApplicationRegistrationRequest() throws Exception {

        RegistrationRequestDTO registrationRequestDTO = new RegistrationRequestDTO();
        registrationRequestDTO.setClientName(clientName);
        registrationRequestDTO.setRedirectUris(redirectUris);
        registrationRequestDTO.setGrantTypes(grantTypes);
        Assert.assertNotNull(DCRMUtils.getApplicationRegistrationRequest(registrationRequestDTO));
        Assert.assertEquals(DCRMUtils.getApplicationRegistrationRequest
                (registrationRequestDTO).getClientName(), clientName);
        Assert.assertEquals(DCRMUtils.getApplicationRegistrationRequest
                (registrationRequestDTO).getGrantTypes(), grantTypes);
        Assert.assertEquals(DCRMUtils.getApplicationRegistrationRequest
                (registrationRequestDTO).getRedirectUris(), redirectUris);
    }

    @Test
    public void testGetApplicationUpdateRequest() throws Exception {

        UpdateRequestDTO updateRequestDTO = new UpdateRequestDTO();
        updateRequestDTO.setClientName(clientName);
        updateRequestDTO.setRedirectUris(redirectUris);
        updateRequestDTO.setGrantTypes(grantTypes);
        Assert.assertNotNull(DCRMUtils.getApplicationUpdateRequest(updateRequestDTO));
        Assert.assertEquals(DCRMUtils.getApplicationUpdateRequest
                (updateRequestDTO).getClientName(), clientName);
        Assert.assertEquals(DCRMUtils.getApplicationUpdateRequest
                (updateRequestDTO).getGrantTypes(), grantTypes);
        Assert.assertEquals(DCRMUtils.getApplicationUpdateRequest
                (updateRequestDTO).getRedirectUris(), redirectUris);
    }

    @DataProvider(name = "BuildDCRMException")
    public Object[][] buildDCRMException() {

        DCRMException dcrmException1 = new DCRMException(null, "error code null");
        DCRMException dcrmException2 = new DCRMException("CONFLICT_", "error code start with conflict");
        DCRMException dcrmException3 =
                new DCRMException("BAD_REQUEST_INVALID_REDIRECT_URI", "error code for invalid redirect URI");
        DCRMException dcrmException4 = new DCRMException("NOT_FOUND_", "error code start with not found");
        DCRMException dcrmException5 = new DCRMException("BAD_REQUEST_", "error code start with bad request");
        DCRMException dcrmException6 =
                new DCRMException("invalid_redirect_uri", "error code for invalid redirect URI");
        return new Object[][]{
                {dcrmException1},
                {dcrmException2},
                {dcrmException3},
                {dcrmException4},
                {dcrmException5},
                {dcrmException6}
        };
    }

    @Test(dataProvider = "BuildDCRMException", expectedExceptions = DCRMEndpointException.class)
    public void testHandleErrorResponse(DCRMException dcrmException) throws Exception {

        Log log = null;
        DCRMUtils.handleErrorResponse(dcrmException, log);
    }

    @DataProvider(name = "BuildDCRMEndpointException")
    public Object[][] buildDCRMEndpointException() {

        Response.Status status = Response.Status.BAD_REQUEST;
        Log log = LogFactory.getLog(DCRMUtils.class);
        Throwable throwable1 =
                new DCRMException("BAD_REQUEST_INVALID_REDIRECT_URI", "error code for invalid redirect URI");
        Throwable throwable2 = new RuntimeException("BAD_REQUEST_INVALID_REDIRECT_URI");

        return new Object[][]{
                {status, throwable1, true, log},
                {status, throwable1, false, log},
                {status, throwable2, true, log},
                {status, throwable2, false, log},
                {status, null, true, log}
        };
    }

    @Test(dataProvider = "BuildDCRMEndpointException", expectedExceptions = DCRMEndpointException.class)
    public void testHandleErrorResponseWithThrowable(Response.Status status, Throwable throwable,
                                                     boolean isServerException, Log log) throws Exception {

        DCRMUtils.handleErrorResponse(status, throwable, isServerException, log);
    }

}
