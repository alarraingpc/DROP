
package org.drip.xva.netting;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2018 Lakshmi Krishnamurthy
 * Copyright (C) 2017 Lakshmi Krishnamurthy
 * 
 *  This file is part of DRIP, a free-software/open-source library for buy/side financial/trading model
 *  	libraries targeting analysts and developers
 *  	https://lakshmidrip.github.io/DRIP/
 *  
 *  DRIP is composed of four main libraries:
 *  
 *  - DRIP Fixed Income - https://lakshmidrip.github.io/DRIP-Fixed-Income/
 *  - DRIP Asset Allocation - https://lakshmidrip.github.io/DRIP-Asset-Allocation/
 *  - DRIP Numerical Optimizer - https://lakshmidrip.github.io/DRIP-Numerical-Optimizer/
 *  - DRIP Statistical Learning - https://lakshmidrip.github.io/DRIP-Statistical-Learning/
 * 
 *  - DRIP Fixed Income: Library for Instrument/Trading Conventions, Treasury Futures/Options,
 *  	Funding/Forward/Overnight Curves, Multi-Curve Construction/Valuation, Collateral Valuation and XVA
 *  	Metric Generation, Calibration and Hedge Attributions, Statistical Curve Construction, Bond RV
 *  	Metrics, Stochastic Evolution and Option Pricing, Interest Rate Dynamics and Option Pricing, LMM
 *  	Extensions/Calibrations/Greeks, Algorithmic Differentiation, and Asset Backed Models and Analytics.
 * 
 *  - DRIP Asset Allocation: Library for model libraries for MPT framework, Black Litterman Strategy
 *  	Incorporator, Holdings Constraint, and Transaction Costs.
 * 
 *  - DRIP Numerical Optimizer: Library for Numerical Optimization and Spline Functionality.
 * 
 *  - DRIP Statistical Learning: Library for Statistical Evaluation and Machine Learning.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   	you may not use this file except in compliance with the License.
 *   
 *  You may obtain a copy of the License at
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  	distributed under the License is distributed on an "AS IS" BASIS,
 *  	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 *  See the License for the specific language governing permissions and
 *  	limitations under the License.
 */

/**
 * FundingGroupPath holds up the Strategy Abstract Realizations of the Sequence in a Single Path Projection
 *  Run over Multiple Collateral Groups onto a Single Funding Group - the Purpose being to calculate Funding
 *  Valuation Adjustments. The References are:
 *  
 *  - Burgard, C., and M. Kjaer (2014): PDE Representations of Derivatives with Bilateral Counter-party Risk
 *  	and Funding Costs, Journal of Credit Risk, 7 (3) 1-19.
 *  
 *  - Burgard, C., and M. Kjaer (2014): In the Balance, Risk, 24 (11) 72-75.
 *  
 *  - Gregory, J. (2009): Being Two-faced over Counter-party Credit Risk, Risk 20 (2) 86-90.
 *  
 *  - Li, B., and Y. Tang (2007): Quantitative Analysis, Derivatives Modeling, and Trading Strategies in the
 *  	Presence of Counter-party Credit Risk for the Fixed Income Market, World Scientific Publishing,
 *  	Singapore.
 * 
 *  - Piterbarg, V. (2010): Funding Beyond Discounting: Collateral Agreements and Derivatives Pricing, Risk
 *  	21 (2) 97-102.
 * 
 * @author Lakshmi Krishnamurthy
 */

public abstract class FundingGroupPath
{
	private org.drip.exposure.universe.MarketPath _marketPath = null;
	private org.drip.xva.netting.CreditDebtGroupPath[] _creditDebtGroupPathArray = null;

	protected FundingGroupPath (
		final org.drip.xva.netting.CreditDebtGroupPath[] creditDebtGroupPathArray,
		final org.drip.exposure.universe.MarketPath marketPath)
		throws java.lang.Exception
	{
		if (null == (_creditDebtGroupPathArray = creditDebtGroupPathArray) ||
			null == (_marketPath = marketPath))
		{
			throw new java.lang.Exception ("FundingGroupPath Constructor => Invalid Inputs");
		}
	}

	/**
	 * Retrieve the Array of CreditDebtGroupPath
	 * 
	 * @return The Array of CreditDebtGroupPath
	 */

	public org.drip.xva.netting.CreditDebtGroupPath[] creditDebtGroupPathArray()
	{
		return _creditDebtGroupPathArray;
	}

	/**
	 * Retrieve the Market Path
	 * 
	 * @return The Market Path
	 */

	public org.drip.exposure.universe.MarketPath marketPath()
	{
		return _marketPath;
	}

	/**
	 * Retrieve the Array of the Vertex Anchor Dates
	 * 
	 * @return The Array of the Vertex Anchor Dates
	 */

	public org.drip.analytics.date.JulianDate[] vertexDates()
	{
		return _creditDebtGroupPathArray[0].vertexDates();
	}

	/**
	 * Compute Path Symmetric Funding Value Spread 01
	 * 
	 * @return The Path Symmetric Funding Value Spread 01
	 */

	public double symmetricFundingValueSpread01()
	{
		double symmetricFundingSpread01 = 0.;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			symmetricFundingSpread01 +=
				_creditDebtGroupPathArray[creditDebtGroupIndex].symmetricFundingValueSpread01();
		}

		return symmetricFundingSpread01;
	}

	/**
	 * Compute Path Unilateral Funding Value Spread 01
	 * 
	 * @return The Path Unilateral Funding Value Spread 01
	 */

	public double unilateralFundingValueSpread01()
	{
		double unilateralFundingValueSpread01 = 0.;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			unilateralFundingValueSpread01 +=
				_creditDebtGroupPathArray[creditDebtGroupIndex].unilateralFundingValueSpread01();
		}

		return 0. > unilateralFundingValueSpread01 ? 0. : unilateralFundingValueSpread01;
	}

	/**
	 * Compute Path Bilateral Funding Value Spread 01
	 * 
	 * @return The Path Bilateral Funding Value Spread 01
	 */

	public double bilateralFundingValueSpread01()
	{
		double bilateralFundingValueSpread01 = 0.;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			bilateralFundingValueSpread01 +=
				_creditDebtGroupPathArray[creditDebtGroupIndex].bilateralFundingValueSpread01();
		}

		return 0. > bilateralFundingValueSpread01 ? 0. : bilateralFundingValueSpread01;
	}

	/**
	 * Compute Period Symmetric Funding Value Spread 01
	 * 
	 * @return The Period Symmetric Funding Value Spread 01
	 */

	public double[] periodSymmetricFundingValueSpread01()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int periodCount = marketVertexArray.length - 1;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;
		double[] periodSymmetricFundingValueSpread01 = new double[periodCount];

		for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
		{
			periodSymmetricFundingValueSpread01[periodIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] periodCreditDebtGroupSymmetricFundingValueSpread01 =
				_creditDebtGroupPathArray[creditDebtGroupIndex].periodSymmetricFundingValueSpread01();

			for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
			{
				periodSymmetricFundingValueSpread01[periodIndex] +=
					periodCreditDebtGroupSymmetricFundingValueSpread01[periodIndex];
			}
		}

		return periodSymmetricFundingValueSpread01;
	}

	/**
	 * Compute Period Unilateral Funding Value Spread 01
	 * 
	 * @return The Period Unilateral Funding Value Spread 01
	 */

	public double[] periodUnilateralFundingValueSpread01()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int periodCount = marketVertexArray.length - 1;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;
		double[] periodUnilateralFundingValueSpread01 = new double[periodCount];

		for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
		{
			periodUnilateralFundingValueSpread01[periodIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] periodCreditDebtGroupUnilateralFundingValueSpread01 =
				_creditDebtGroupPathArray[creditDebtGroupIndex].periodUnilateralFundingValueSpread01();

			for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
			{
				periodUnilateralFundingValueSpread01[periodIndex] +=
					periodCreditDebtGroupUnilateralFundingValueSpread01[periodIndex];
			}
		}

		for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
		{
			if (0. > periodUnilateralFundingValueSpread01[periodIndex])
			{
				periodUnilateralFundingValueSpread01[periodIndex] = 0.;
			}
		}

		return periodUnilateralFundingValueSpread01;
	}

	/**
	 * Compute Period Bilateral Funding Value Spread 01
	 * 
	 * @return The Period Bilateral Funding Value Spread 01
	 */

	public double[] periodBilateralFundingValueSpread01()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int periodCount = marketVertexArray.length - 1;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;
		double[] periodBilateralFundingValueSpread01 = new double[periodCount];

		for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
		{
			periodBilateralFundingValueSpread01[periodIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] periodCreditDebtGroupBilateralFundingValueSpread01 =
				_creditDebtGroupPathArray[creditDebtGroupIndex].periodBilateralFundingValueSpread01();

			for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
			{
				periodBilateralFundingValueSpread01[periodIndex] +=
					periodCreditDebtGroupBilateralFundingValueSpread01[periodIndex];
			}
		}

		for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
		{
			if (0. > periodBilateralFundingValueSpread01[periodIndex])
			{
				periodBilateralFundingValueSpread01[periodIndex] = 0.;
			}
		}

		return periodBilateralFundingValueSpread01;
	}

	/**
	 * Compute Path Symmetric Funding Value Adjustment
	 * 
	 * @return The Path Symmetric Funding Value Adjustment
	 */

	public double symmetricFundingValueAdjustment()
	{
		double[] periodSymmetricFundingValueSpread01 = periodSymmetricFundingValueSpread01();

		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int periodCount = periodSymmetricFundingValueSpread01.length;
		double symmetricFundingValueAdjustment = 0.;

		for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
		{
			symmetricFundingValueAdjustment +=
				0.5 * periodSymmetricFundingValueSpread01[periodIndex] * (
					marketVertexArray[periodIndex].dealer().seniorFundingSpread() +
					marketVertexArray[periodIndex + 1].dealer().seniorFundingSpread()
				);
		}

		return symmetricFundingValueAdjustment;
	}

	/**
	 * Compute Path Unilateral Funding Value Adjustment
	 * 
	 * @return The Path Unilateral Funding Value Adjustment
	 */

	public double unilateralFundingValueAdjustment()
	{
		double[] periodUnilateralFundingValueSpread01 = periodUnilateralFundingValueSpread01();

		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int periodCount = periodUnilateralFundingValueSpread01.length;
		double unilateralFundingValueAdjustment = 0.;

		for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
		{
			unilateralFundingValueAdjustment -=
				0.5 * periodUnilateralFundingValueSpread01[periodIndex] * (
					marketVertexArray[periodIndex].dealer().seniorFundingSpread() +
					marketVertexArray[periodIndex + 1].dealer().seniorFundingSpread()
				);
		}

		return unilateralFundingValueAdjustment;
	}

	/**
	 * Compute Path Bilateral Funding Value Adjustment
	 * 
	 * @return The Path Bilateral Funding Value Adjustment
	 */

	public double bilateralFundingValueAdjustment()
	{
		double[] periodBilateralFundingValueSpread01 = periodBilateralFundingValueSpread01();

		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int periodCount = periodBilateralFundingValueSpread01.length;
		double bilateralFundingValueAdjustment = 0.;

		for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
		{
			bilateralFundingValueAdjustment -=
				0.5 * periodBilateralFundingValueSpread01[periodIndex] * (
					marketVertexArray[periodIndex].dealer().seniorFundingSpread() +
					marketVertexArray[periodIndex + 1].dealer().seniorFundingSpread()
				);
		}

		return bilateralFundingValueAdjustment;
	}

	/**
	 * Compute Path Unilateral Funding Debt Adjustment
	 * 
	 * @return The Path Unilateral Funding Debt Adjustment
	 */

	public double unilateralFundingDebtAdjustment()
	{
		double[] periodUnilateralFundingDebtAdjustment = periodUnilateralFundingDebtAdjustment();

		int periodCount = periodUnilateralFundingDebtAdjustment.length;
		double unilateralFundingDebtAdjustment = 0.;

		for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
		{
			unilateralFundingDebtAdjustment += periodUnilateralFundingDebtAdjustment[periodIndex];
		}

		return unilateralFundingDebtAdjustment;
	}

	/**
	 * Compute Path Bilateral Funding Debt Adjustment
	 * 
	 * @return The Path Bilateral Funding Debt Adjustment
	 */

	public double bilateralFundingDebtAdjustment()
	{
		double[] periodBilateralFundingDebtAdjustment = periodBilateralFundingDebtAdjustment();

		int periodCount = periodBilateralFundingDebtAdjustment.length;
		double bilateralFundingDebtAdjustment = 0.;

		for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
		{
			bilateralFundingDebtAdjustment += periodBilateralFundingDebtAdjustment[periodIndex];
		}

		return bilateralFundingDebtAdjustment;
	}

	/**
	 * Compute Vertex Path Collateralized Exposure
	 * 
	 * @return The Vertex Path Collateralized Exposure
	 */

	public double[] vertexCollateralizedExposure()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int vertexCount = marketVertexArray.length;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;
		double[] vertexCollateralizedExposure = new double[vertexCount];

		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
		{
			vertexCollateralizedExposure[vertexIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] creditDebtGroupVertexCollateralizedExposure =
				_creditDebtGroupPathArray[creditDebtGroupIndex].vertexCollateralizedExposure();

			for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
			{
				vertexCollateralizedExposure[vertexIndex] +=
					creditDebtGroupVertexCollateralizedExposure[vertexIndex];
			}
		}

		return vertexCollateralizedExposure;
	}

	/**
	 * Compute Vertex Path Collateralized Exposure PV
	 * 
	 * @return The Vertex Path Collateralized Exposure PV
	 */

	public double[] vertexCollateralizedExposurePV()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int vertexCount = marketVertexArray.length;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;
		double[] vertexCollateralizedExposurePV = new double[vertexCount];

		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
		{
			vertexCollateralizedExposurePV[vertexIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] creditDebtGroupVertexCollateralizedExposurePV =
				_creditDebtGroupPathArray[creditDebtGroupIndex].vertexCollateralizedExposurePV();

			for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
			{
				vertexCollateralizedExposurePV[vertexIndex] +=
					creditDebtGroupVertexCollateralizedExposurePV[vertexIndex];
			}
		}

		return vertexCollateralizedExposurePV;
	}

	/**
	 * Compute Vertex Path Collateralized Positive Exposure
	 * 
	 * @return The Vertex Path Collateralized Positive Exposure
	 */

	public double[] vertexCollateralizedPositiveExposure()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int vertexCount = marketVertexArray.length;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;
		double[] vertexCollateralizedPositiveExposure = new double[vertexCount];

		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
		{
			vertexCollateralizedPositiveExposure[vertexIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] creditDebtGroupVertexCollateralizedPositiveExposure =
				_creditDebtGroupPathArray[creditDebtGroupIndex].vertexCollateralizedPositiveExposure();

			for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
			{
				vertexCollateralizedPositiveExposure[vertexIndex] +=
					creditDebtGroupVertexCollateralizedPositiveExposure[vertexIndex];
			}
		}

		return vertexCollateralizedPositiveExposure;
	}

	/**
	 * Compute Vertex Path Collateralized Positive Exposure PV
	 * 
	 * @return The Vertex Path Collateralized Positive Exposure PV
	 */

	public double[] vertexCollateralizedPositiveExposurePV()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int vertexCount = marketVertexArray.length;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;
		double[] vertexCollateralizedPositiveExposurePV = new double[vertexCount];

		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
		{
			vertexCollateralizedPositiveExposurePV[vertexIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] creditDebtGroupVertexCollateralizedPositiveExposurePV =
				_creditDebtGroupPathArray[creditDebtGroupIndex].vertexCollateralizedPositiveExposurePV();

			for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
			{
				vertexCollateralizedPositiveExposurePV[vertexIndex] +=
					creditDebtGroupVertexCollateralizedPositiveExposurePV[vertexIndex];
			}
		}

		return vertexCollateralizedPositiveExposurePV;
	}

	/**
	 * Compute Vertex Path Collateralized Negative Exposure
	 * 
	 * @return The Vertex Path Collateralized Negative Exposure
	 */

	public double[] vertexCollateralizedNegativeExposure()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int vertexCount = marketVertexArray.length;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;
		double[] vertexCollateralizedNegativeExposure = new double[vertexCount];

		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
		{
			vertexCollateralizedNegativeExposure[vertexIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] creditDebtGroupVertexCollateralizedNegativeExposure =
				_creditDebtGroupPathArray[creditDebtGroupIndex].vertexCollateralizedNegativeExposure();

			for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
			{
				vertexCollateralizedNegativeExposure[vertexIndex] +=
					creditDebtGroupVertexCollateralizedNegativeExposure[vertexIndex];
			}
		}

		return vertexCollateralizedNegativeExposure;
	}

	/**
	 * Compute Vertex Path Collateralized Negative Exposure PV
	 * 
	 * @return The Vertex Path Collateralized Negative Exposure PV
	 */

	public double[] vertexCollateralizedNegativeExposurePV()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int vertexCount = marketVertexArray.length;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;
		double[] vertexCollateralizedNegativeExposurePV = new double[vertexCount];

		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
		{
			vertexCollateralizedNegativeExposurePV[vertexIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] creditDebtGroupVertexCollateralizedNegativeExposurePV =
				_creditDebtGroupPathArray[creditDebtGroupIndex].vertexCollateralizedNegativeExposurePV();

			for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
			{
				vertexCollateralizedNegativeExposurePV[vertexIndex] +=
					creditDebtGroupVertexCollateralizedNegativeExposurePV[vertexIndex];
			}
		}

		return vertexCollateralizedNegativeExposurePV;
	}

	/**
	 * Compute Vertex Path Uncollateralized Exposure
	 * 
	 * @return The Vertex Path Uncollateralized Exposure
	 */

	public double[] vertexUncollateralizedExposure()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int vertexCount = marketVertexArray.length;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;
		double[] vertexUncollateralizedExposure = new double[vertexCount];

		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
		{
			vertexUncollateralizedExposure[vertexIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] creditDebtGroupVertexCollateralizedExposure =
				_creditDebtGroupPathArray[creditDebtGroupIndex].vertexUncollateralizedExposure();

			for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
			{
				vertexUncollateralizedExposure[vertexIndex] +=
					creditDebtGroupVertexCollateralizedExposure[vertexIndex];
			}
		}

		return vertexUncollateralizedExposure;
	}

	/**
	 * Compute Vertex Path Uncollateralized Exposure PV
	 * 
	 * @return The Vertex Path Uncollateralized Exposure PV
	 */

	public double[] vertexUncollateralizedExposurePV()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int vertexCount = marketVertexArray.length;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;
		double[] vertexUncollateralizedExposurePV = new double[vertexCount];

		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
		{
			vertexUncollateralizedExposurePV[vertexIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] creditDebtGroupVertexCollateralizedExposurePV =
				_creditDebtGroupPathArray[creditDebtGroupIndex].vertexUncollateralizedExposurePV();

			for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
			{
				vertexUncollateralizedExposurePV[vertexIndex] +=
					creditDebtGroupVertexCollateralizedExposurePV[vertexIndex];
			}
		}

		return vertexUncollateralizedExposurePV;
	}

	/**
	 * Compute Vertex Path Uncollateralized Positive Exposure
	 * 
	 * @return The Vertex Path Uncollateralized Positive Exposure
	 */

	public double[] vertexUncollateralizedPositiveExposure()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int vertexCount = marketVertexArray.length;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;
		double[] vertexUncollateralizedPositiveExposure = new double[vertexCount];

		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
		{
			vertexUncollateralizedPositiveExposure[vertexIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] creditDebtGroupVertexUncollateralizedPositiveExposure =
				_creditDebtGroupPathArray[creditDebtGroupIndex].vertexUncollateralizedPositiveExposure();

			for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
			{
				vertexUncollateralizedPositiveExposure[vertexIndex] +=
					creditDebtGroupVertexUncollateralizedPositiveExposure[vertexIndex];
			}
		}

		return vertexUncollateralizedPositiveExposure;
	}

	/**
	 * Compute Vertex Path Uncollateralized Positive Exposure PV
	 * 
	 * @return The Vertex Path Uncollateralized Positive Exposure PV
	 */

	public double[] vertexUncollateralizedPositiveExposurePV()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int vertexCount = marketVertexArray.length;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;
		double[] vertexUncollateralizedPositiveExposurePV = new double[vertexCount];

		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
		{
			vertexUncollateralizedPositiveExposurePV[vertexIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] creditDebtGroupVertexUncollateralizedPositiveExposurePV =
				_creditDebtGroupPathArray[creditDebtGroupIndex].vertexUncollateralizedPositiveExposurePV();

			for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
			{
				vertexUncollateralizedPositiveExposurePV[vertexIndex] +=
					creditDebtGroupVertexUncollateralizedPositiveExposurePV[vertexIndex];
			}
		}

		return vertexUncollateralizedPositiveExposurePV;
	}

	/**
	 * Compute Vertex Path Uncollateralized Negative Exposure
	 * 
	 * @return The Vertex Path Uncollateralized Negative Exposure
	 */

	public double[] vertexUncollateralizedNegativeExposure()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int vertexCount = marketVertexArray.length;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;
		double[] vertexUncollateralizedNegativeExposure = new double[vertexCount];

		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
		{
			vertexUncollateralizedNegativeExposure[vertexIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] creditDebtGroupVertexUncollateralizedNegativeExposure =
				_creditDebtGroupPathArray[creditDebtGroupIndex].vertexUncollateralizedNegativeExposure();

			for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
			{
				vertexUncollateralizedNegativeExposure[vertexIndex] +=
					creditDebtGroupVertexUncollateralizedNegativeExposure[vertexIndex];
			}
		}

		return vertexUncollateralizedNegativeExposure;
	}

	/**
	 * Compute Vertex Path Uncollateralized Negative Exposure PV
	 * 
	 * @return The Vertex Path Uncollateralized Negative Exposure PV
	 */

	public double[] vertexUncollateralizedNegativeExposurePV()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int vertexCount = marketVertexArray.length;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;
		double[] vertexUncollateralizedNegativeExposurePV = new double[vertexCount];

		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
		{
			vertexUncollateralizedNegativeExposurePV[vertexIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] creditDebtGroupVertexUncollateralizedNegativeExposurePV =
				_creditDebtGroupPathArray[creditDebtGroupIndex].vertexUncollateralizedNegativeExposurePV();

			for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
			{
				vertexUncollateralizedNegativeExposurePV[vertexIndex] +=
					creditDebtGroupVertexUncollateralizedNegativeExposurePV[vertexIndex];
			}
		}

		return vertexUncollateralizedNegativeExposurePV;
	}

	/**
	 * Compute Vertex Path Funding Exposure
	 * 
	 * @return The Vertex Path Funding Exposure
	 */

	public double[] vertexFundingExposure()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int vertexCount = marketVertexArray.length;
		double[] vertexFundingExposure = new double[vertexCount];
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;

		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
		{
			vertexFundingExposure[vertexIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] creditDebtGroupVertexFundingExposure =
				_creditDebtGroupPathArray[creditDebtGroupIndex].vertexFundingExposure();

			for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
			{
				vertexFundingExposure[vertexIndex] += creditDebtGroupVertexFundingExposure[vertexIndex];
			}
		}

		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
		{
			if (0. > vertexFundingExposure[vertexIndex])
			{
				vertexFundingExposure[vertexIndex] = 0.;
			}
		}

		return vertexFundingExposure;
	}

	/**
	 * Compute Vertex Path Funding Exposure PV
	 * 
	 * @return The Vertex Path Funding Exposure PV
	 */

	public double[] vertexFundingExposurePV()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int vertexCount = marketVertexArray.length;
		double[] vertexFundingExposurePV = new double[vertexCount];
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;

		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
		{
			vertexFundingExposurePV[vertexIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] creditDebtGroupVertexFundingExposurePV =
				_creditDebtGroupPathArray[creditDebtGroupIndex].vertexFundingExposurePV();

			for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
			{
				vertexFundingExposurePV[vertexIndex] += creditDebtGroupVertexFundingExposurePV[vertexIndex];
			}
		}

		for (int vertexIndex = 0; vertexIndex < vertexCount; ++vertexIndex)
		{
			if (0. > vertexFundingExposurePV[vertexIndex])
			{
				vertexFundingExposurePV[vertexIndex] = 0.;
			}
		}

		return vertexFundingExposurePV;
	}

	/**
	 * Compute Period-wise Path Symmetric Funding Value Adjustment
	 * 
	 * @return The Period-wise Path Symmetric Funding Value Adjustment
	 */

	public double[] periodSymmetricFundingValueAdjustment()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		double[] periodSymmetricFundingValueSpread01 = periodSymmetricFundingValueSpread01();

		int periodCount = periodSymmetricFundingValueSpread01.length;
		double[] periodSymmetricFundingValueAdjustment = new double[periodCount];

		for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
		{
			periodSymmetricFundingValueAdjustment[periodIndex] =
				0.5 * periodSymmetricFundingValueSpread01[periodIndex] * (
					marketVertexArray[periodIndex].dealer().seniorFundingSpread() +
					marketVertexArray[periodIndex + 1].dealer().seniorFundingSpread()
				);
		}

		return periodSymmetricFundingValueAdjustment;
	}

	/**
	 * Compute Period-wise Unilateral Path Funding Value Adjustment
	 * 
	 * @return The Period-wise Unilateral Path Funding Value Adjustment
	 */

	public double[] periodUnilateralFundingValueAdjustment()
	{
		double[] periodUnilateralFundingValueSpread01 = periodUnilateralFundingValueSpread01();

		int periodCount = periodUnilateralFundingValueSpread01.length;
		double[] periodUnilateralFundingValueAdjustment = new double[periodCount];

		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
		{
			periodUnilateralFundingValueAdjustment[periodIndex] =
				0.5 * periodUnilateralFundingValueSpread01[periodIndex] * (
					marketVertexArray[periodIndex].dealer().seniorFundingSpread() +
					marketVertexArray[periodIndex + 1].dealer().seniorFundingSpread()
				);
		}

		return periodUnilateralFundingValueAdjustment;
	}

	/**
	 * Compute Period-wise Bilateral Path Funding Value Adjustment
	 * 
	 * @return The Period-wise Bilateral Path Funding Value Adjustment
	 */

	public double[] periodBilateralFundingValueAdjustment()
	{
		double[] periodBilateralFundingValueSpread01 = periodBilateralFundingValueSpread01();

		int periodCount = periodBilateralFundingValueSpread01.length;
		double[] periodBilateralFundingValueAdjustment = new double[periodCount];

		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
		{
			periodBilateralFundingValueAdjustment[periodIndex] =
				0.5 * periodBilateralFundingValueSpread01[periodIndex] * (
					marketVertexArray[periodIndex].dealer().seniorFundingSpread() +
					marketVertexArray[periodIndex + 1].dealer().seniorFundingSpread()
				);
		}

		return periodBilateralFundingValueAdjustment;
	}

	/**
	 * Compute Period-wise Path Unilateral Funding Debt Adjustment
	 * 
	 * @return The Period-wise Path Unilateral Funding Debt Adjustment
	 */

	public double[] periodUnilateralFundingDebtAdjustment()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int periodCount = marketVertexArray.length - 1;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;
		double[] periodUnilateralFundingDebtAdjustment = new double[periodCount];

		for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
		{
			periodUnilateralFundingDebtAdjustment[periodIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] periodUnilateralFundingDebtAdjustmentCreditDebtGroup =
				_creditDebtGroupPathArray[creditDebtGroupIndex].periodUnilateralFundingDebtAdjustment();

			for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
			{
				periodUnilateralFundingDebtAdjustment[periodIndex] +=
					periodUnilateralFundingDebtAdjustmentCreditDebtGroup[periodIndex];
			}
		}

		return periodUnilateralFundingDebtAdjustment;
	}

	/**
	 * Compute Period-wise Path Bilateral Funding Debt Adjustment
	 * 
	 * @return The Period-wise Path Bilateral Funding Debt Adjustment
	 */

	public double[] periodBilateralFundingDebtAdjustment()
	{
		org.drip.exposure.universe.MarketVertex[] marketVertexArray = _marketPath.marketVertexArray();

		int periodCount = marketVertexArray.length - 1;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;
		double[] periodBilateralFundingDebtAdjustment = new double[periodCount];

		for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
		{
			periodBilateralFundingDebtAdjustment[periodIndex] = 0.;
		}

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			double[] periodBilateralFundingDebtAdjustmentCreditDebtGroup =
				_creditDebtGroupPathArray[creditDebtGroupIndex].periodBilateralFundingDebtAdjustment();

			for (int periodIndex = 0; periodIndex < periodCount; ++periodIndex)
			{
				periodBilateralFundingDebtAdjustment[periodIndex] +=
					periodBilateralFundingDebtAdjustmentCreditDebtGroup[periodIndex];
			}
		}

		return periodBilateralFundingDebtAdjustment;
	}

	/**
	 * Compute Path Unilateral Credit Value Adjustment
	 * 
	 * @return The Path Unilateral Credit Value Adjustment
	 */

	public double unilateralCreditAdjustment()
	{
		double unilateralCreditAdjustment = 0.;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			unilateralCreditAdjustment +=
				_creditDebtGroupPathArray[creditDebtGroupIndex].unilateralCreditAdjustment();
		}

		return unilateralCreditAdjustment;
	}

	/**
	 * Compute Path Bilateral Credit Value Adjustment
	 * 
	 * @return The Path Bilateral Credit Value Adjustment
	 */

	public double bilateralCreditAdjustment()
	{
		double bilateralCreditAdjustment = 0.;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			bilateralCreditAdjustment +=
				_creditDebtGroupPathArray[creditDebtGroupIndex].bilateralCreditAdjustment();
		}

		return bilateralCreditAdjustment;
	}

	/**
	 * Compute Path Contra-Liability Credit Adjustment
	 * 
	 * @return The Path Contra-Liability Credit Adjustment
	 */

	public double contraLiabilityCreditAdjustment()
	{
		double contraLiabilityCreditAdjustment = 0.;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			contraLiabilityCreditAdjustment +=
				_creditDebtGroupPathArray[creditDebtGroupIndex].contraLiabilityCreditAdjustment();
		}

		return contraLiabilityCreditAdjustment;
	}

	/**
	 * Compute Path Unilateral Debt Value Adjustment
	 * 
	 * @return The Path Unilateral Debt Value Adjustment
	 */

	public double unilateralDebtAdjustment()
	{
		double unilateralDebtAdjustment = 0.;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			unilateralDebtAdjustment +=
				_creditDebtGroupPathArray[creditDebtGroupIndex].unilateralDebtAdjustment();
		}

		return unilateralDebtAdjustment;
	}

	/**
	 * Compute Path Bilateral Debt Value Adjustment
	 * 
	 * @return The Path Bilateral Credit Value Adjustment
	 */

	public double bilateralDebtAdjustment()
	{
		double bilateralDebtAdjustment = 0.;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			bilateralDebtAdjustment +=
				_creditDebtGroupPathArray[creditDebtGroupIndex].bilateralDebtAdjustment();
		}

		return bilateralDebtAdjustment;
	}

	/**
	 * Compute Path Contra-Asset Debt Adjustment
	 * 
	 * @return The Path Contra-Asset Debt Adjustment
	 */

	public double contraAssetDebtAdjustment()
	{
		double contraAssetDebtAdjustment = 0.;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			contraAssetDebtAdjustment +=
				_creditDebtGroupPathArray[creditDebtGroupIndex].contraAssetDebtAdjustment();
		}

		return contraAssetDebtAdjustment;
	}

	/**
	 * Compute Path Unilateral Collateral Value Adjustment
	 * 
	 * @return The Path Unilateral Collateral Value Adjustment
	 */

	public double unilateralCollateralAdjustment()
	{
		double unilateralCollateralAdjustment = 0.;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			unilateralCollateralAdjustment +=
				_creditDebtGroupPathArray[creditDebtGroupIndex].unilateralCollateralAdjustment();
		}

		return unilateralCollateralAdjustment;
	}

	/**
	 * Compute Path Bilateral Collateral Value Adjustment
	 * 
	 * @return The Path Bilateral Collateral Value Adjustment
	 */

	public double bilateralCollateralAdjustment()
	{
		double bilateralCollateralAdjustment = 0.;
		int creditDebtGroupCount = _creditDebtGroupPathArray.length;

		for (int creditDebtGroupIndex = 0; creditDebtGroupIndex < creditDebtGroupCount;
			++creditDebtGroupIndex)
		{
			bilateralCollateralAdjustment +=
				_creditDebtGroupPathArray[creditDebtGroupIndex].bilateralCollateralAdjustment();
		}

		return bilateralCollateralAdjustment;
	}

	/**
	 * Compute Path Funding Value Adjustment
	 * 
	 * @return The Path Funding Value Adjustment
	 */

	public abstract double fundingValueAdjustment();

	/**
	 * Compute Path Funding Debt Adjustment
	 * 
	 * @return The Path Funding Debt Adjustment
	 */

	public abstract double fundingDebtAdjustment();

	/**
	 * Compute Path Funding Cost Adjustment
	 * 
	 * @return The Path Funding Cost Adjustment
	 */

	public abstract double fundingCostAdjustment();

	/**
	 * Compute Path Funding Benefit Adjustment
	 * 
	 * @return The Path Funding Benefit Adjustment
	 */

	public abstract double fundingBenefitAdjustment();

	/**
	 * Compute Period-wise Path Funding Value Adjustment
	 * 
	 * @return The Period-wise Path Funding Value Adjustment
	 */

	public abstract double[] periodFundingValueAdjustment();

	/**
	 * Compute Period-wise Path Funding Debt Adjustment
	 * 
	 * @return The Period-wise Path Funding Debt Adjustment
	 */

	public abstract double[] periodFundingDebtAdjustment();

	/**
	 * Compute Period-wise Path Funding Cost Adjustment
	 * 
	 * @return The Period-wise Path Funding Cost Adjustment
	 */

	public abstract double[] periodFundingCostAdjustment();

	/**
	 * Compute Period-wise Path Funding Benefit Adjustment
	 * 
	 * @return The Period-wise Path Funding Benefit Adjustment
	 */

	public abstract double[] periodFundingBenefitAdjustment();
}
