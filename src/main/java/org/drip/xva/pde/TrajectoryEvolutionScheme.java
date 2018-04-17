
package org.drip.xva.pde;

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
 * TrajectoryEvolutionScheme holds the Evolution Edges of a Trajectory evolved in a Dynamically Adaptive
 *  Manner, as laid out in Burgard and Kjaer (2014). The References are:
 *  
 *  - Burgard, C., and M. Kjaer (2014): PDE Representations of Derivatives with Bilateral Counter-party Risk
 *  	and Funding Costs, Journal of Credit Risk, 7 (3) 1-19.
 *  
 *  - Cesari, G., J. Aquilina, N. Charpillon, X. Filipovic, G. Lee, and L. Manda (2009): Modeling, Pricing,
 *  	and Hedging Counter-party Credit Exposure - A Technical Guide, Springer Finance, New York.
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

public class TrajectoryEvolutionScheme
{
	private org.drip.exposure.evolver.PrimarySecurityDynamicsContainer _tradeablesContainer = null;
	private org.drip.xva.definition.PDEEvolutionControl _pdeEvolutionControl = null;

	/**
	 * TrajectoryEvolutionScheme Constructor
	 * 
	 * @param tradeablesContainer The Universe of Tradeables
	 * @param pdeEvolutionControl The XVA PDE Control Settings
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public TrajectoryEvolutionScheme (
		final org.drip.exposure.evolver.PrimarySecurityDynamicsContainer tradeablesContainer,
		final org.drip.xva.definition.PDEEvolutionControl pdeEvolutionControl)
		throws java.lang.Exception
	{
		if (null == (_tradeablesContainer = tradeablesContainer) ||
			null == (_pdeEvolutionControl = pdeEvolutionControl))
		{
			throw new java.lang.Exception ("TrajectoryEvolutionScheme Constructor => Invalid Inputs");
		}
	}

	/**
	 * Retrieve the Universe of Tradeables
	 * 
	 * @return The Universe of Tradeables
	 */

	public org.drip.exposure.evolver.PrimarySecurityDynamicsContainer tradeablesContainer()
	{
		return _tradeablesContainer;
	}

	/**
	 * Retrieve the XVA PDE Control Settings
	 * 
	 * @return The XVA PDE Control Settings
	 */

	public org.drip.xva.definition.PDEEvolutionControl pdeEvolutionControl()
	{
		return _pdeEvolutionControl;
	}

	/**
	 * Re-balance the Cash Account and generate the Derivative Value Update
	 * 
	 * @param initialTrajectoryVertex The Starting Evolution Trajectory Vertex
	 * @param marketEdge Market Edge Instance
	 * 
	 * @return The CashAccountRebalancer Instance
	 */

	public org.drip.xva.derivative.CashAccountRebalancer rebalanceCash (
		final org.drip.xva.derivative.EvolutionTrajectoryVertex initialTrajectoryVertex,
		final org.drip.exposure.universe.MarketEdge marketEdge)
	{
		if (null == initialTrajectoryVertex ||
			null == marketEdge)
		{
			return null;
		}

		org.drip.xva.derivative.ReplicationPortfolioVertex initialReplicationPortfolioVertex =
			initialTrajectoryVertex.replicationPortfolioVertex();

		double initialPortfolioHoldings = initialReplicationPortfolioVertex.positionHoldings();

		double initialDealerSeniorNumeraireHoldings =
			initialReplicationPortfolioVertex.dealerSeniorNumeraireHoldings();

		double initialClientNumeraireHoldings = initialReplicationPortfolioVertex.clientNumeraireHoldings();

		double initialDealerSubordinateNumeraireHoldings =
			initialReplicationPortfolioVertex.dealerSubordinateNumeraireHoldings();

		org.drip.exposure.universe.MarketVertex initialMarketVertex = marketEdge.start();

		org.drip.exposure.universe.MarketVertex finalMarketVertex = marketEdge.finish();

		org.drip.exposure.universe.MarketVertexEntity emvDealerStart = initialMarketVertex.dealer();

		org.drip.exposure.universe.MarketVertexEntity dealerMarketVertex = finalMarketVertex.dealer();

		org.drip.exposure.universe.MarketVertexEntity clientMarketVertex = finalMarketVertex.client();

		double finalPortfolioValue = finalMarketVertex.positionManifestValue();

		double finalDealerSeniorFundingNumeraire = dealerMarketVertex.seniorFundingReplicator();

		double finalClientNumeraire = clientMarketVertex.seniorFundingReplicator();

		double initialDealerSubordinateFundingNumeraire = emvDealerStart.subordinateFundingReplicator();

		double finalDealerSubordinateFundingNumeraire = dealerMarketVertex.subordinateFundingReplicator();

		double timeIncrement = marketEdge.vertexIncrement() / 365.25;

		double portfolioCashChange = initialPortfolioHoldings *
			_tradeablesContainer.position().cashAccumulationRate() * finalPortfolioValue * timeIncrement;

		org.drip.exposure.evolver.PrimarySecurity clientFundingTradeable = _tradeablesContainer.clientFunding();

		double clientCashAccumulation = initialClientNumeraireHoldings *
			clientFundingTradeable.cashAccumulationRate() * finalClientNumeraire * timeIncrement;

		double clientHoldingsValueChange = initialClientNumeraireHoldings * (finalClientNumeraire -
			initialMarketVertex.client().seniorFundingReplicator());

		double cashAccountBalance = -1. * initialTrajectoryVertex.positionGreekVertex().derivativeXVAValue()
			- initialDealerSeniorNumeraireHoldings * finalDealerSeniorFundingNumeraire;

		if (org.drip.quant.common.NumberUtil.IsValid (finalDealerSubordinateFundingNumeraire))
		{
			cashAccountBalance -= initialDealerSubordinateNumeraireHoldings *
				finalDealerSubordinateFundingNumeraire;
		}

		org.drip.exposure.evolver.PrimarySecurity csaTradeable = _tradeablesContainer.csa();

		org.drip.exposure.evolver.PrimarySecurity dealerSeniorFundingTradeable =
			_tradeablesContainer.dealerSeniorFunding();

		double dealerCashAccumulation = cashAccountBalance * (cashAccountBalance > 0. ?
			csaTradeable.cashAccumulationRate() : dealerSeniorFundingTradeable.cashAccumulationRate()) *
				timeIncrement;

		double derivativeXVAValueChange = -1. * (initialPortfolioHoldings * (finalPortfolioValue -
			initialMarketVertex.positionManifestValue()) + initialDealerSeniorNumeraireHoldings *
				(finalDealerSeniorFundingNumeraire - emvDealerStart.seniorFundingReplicator()) +
					clientHoldingsValueChange + (portfolioCashChange + clientCashAccumulation +
						dealerCashAccumulation) * timeIncrement);

		if (org.drip.quant.common.NumberUtil.IsValid (initialDealerSubordinateFundingNumeraire) &&
			org.drip.quant.common.NumberUtil.IsValid (finalDealerSubordinateFundingNumeraire))
		{
			derivativeXVAValueChange += initialDealerSubordinateNumeraireHoldings *
				(finalDealerSubordinateFundingNumeraire - initialDealerSubordinateFundingNumeraire);
		}

		try
		{
			return new org.drip.xva.derivative.CashAccountRebalancer (
				new org.drip.xva.derivative.CashAccountEdge (
					portfolioCashChange,
					dealerCashAccumulation * timeIncrement,
					clientCashAccumulation * timeIncrement
				),
				derivativeXVAValueChange
			);
		}
		catch (java.lang.Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Execute a Single Euler Time Step Walk
	 * 
	 * @param marketEdge Market Edge Instance
	 * @param burgardKjaerOperator The Burgard Kjaer Operator Instance
	 * @param initialTrajectoryVertex The Starting ETV Instance
	 * @param collateral The Applicable Collateral
	 * 
	 * @return The Evolution Trajectory Edge
	 */

	public org.drip.xva.derivative.EvolutionTrajectoryEdge eulerWalk (
		final org.drip.exposure.universe.MarketEdge marketEdge,
		final org.drip.xva.pde.BurgardKjaerOperator burgardKjaerOperator,
		final org.drip.xva.derivative.EvolutionTrajectoryVertex initialTrajectoryVertex,
		final double collateral)
	{
		if (null == marketEdge ||
			null == burgardKjaerOperator ||
			null == initialTrajectoryVertex)
		{
			return null;
		}

		org.drip.xva.derivative.PositionGreekVertex initialPositionGreekVertex =
			initialTrajectoryVertex.positionGreekVertex();

		org.drip.xva.pde.BurgardKjaerEdgeRun burgardKjaerEdgeRun = burgardKjaerOperator.edgeRun (
			marketEdge,
			initialTrajectoryVertex,
			collateral
		);

		double initialTime = initialTrajectoryVertex.time();

		double timeIncrement = marketEdge.vertexIncrement() / 365.25;

		if (null == burgardKjaerEdgeRun)
		{
			return null;
		}

		double theta = burgardKjaerEdgeRun.theta();

		double positionValueBump = burgardKjaerEdgeRun.positionValueBump();

		double thetaPositionValueUp = burgardKjaerEdgeRun.thetaPositionValueUp();

		double thetaPositionValueDown = burgardKjaerEdgeRun.thetaPositionValueDown();

		org.drip.exposure.universe.MarketVertex finalMarketVertex = marketEdge.finish();

		org.drip.exposure.universe.MarketVertexEntity dealerMarketVertex = finalMarketVertex.dealer();

		org.drip.exposure.universe.MarketVertexEntity clientMarketVertex = finalMarketVertex.client();

		double derivativeXVAValueDeltaFinish =
			initialPositionGreekVertex.derivativeXVAValueDelta() +
			0.5 * (thetaPositionValueUp - thetaPositionValueDown) * timeIncrement / positionValueBump;

		double clientGainOnDealerDefault = java.lang.Double.NaN;
		double finalGainOnClientDefault = java.lang.Double.NaN;

		double derivativeXVAValueFinish = initialPositionGreekVertex.derivativeXVAValue() - theta *
			timeIncrement;

		try
		{
			org.drip.xva.definition.CloseOut closeOutScheme = new
				org.drip.xva.definition.CloseOutBilateral (
					dealerMarketVertex.seniorRecoveryRate(),
					clientMarketVertex.seniorRecoveryRate()
				);

			clientGainOnDealerDefault = closeOutScheme.dealerDefault (derivativeXVAValueFinish);

			finalGainOnClientDefault = -1. * (derivativeXVAValueFinish - closeOutScheme.clientDefault
				(derivativeXVAValueFinish));
		}
		catch (java.lang.Exception e)
		{
			e.printStackTrace();

			return null;
		}

		double dealerSubordinateFundingNumeraire = dealerMarketVertex.subordinateFundingReplicator();

		double gainOnDealerDefaultFinish = -1. * (derivativeXVAValueFinish - clientGainOnDealerDefault);

		double finalClientHoldings = finalGainOnClientDefault / clientMarketVertex.seniorFundingReplicator();

		org.drip.xva.derivative.CashAccountRebalancer cashAccountRebalancer = rebalanceCash (
			initialTrajectoryVertex,
			marketEdge
		);

		if (null == cashAccountRebalancer)
		{
			return null;
		}

		org.drip.xva.derivative.CashAccountEdge cashAccountEdge = cashAccountRebalancer.cashAccountEdge();

		double dealerSeniorFundingNumeraire = dealerMarketVertex.seniorFundingReplicator();

		org.drip.exposure.evolver.PrimarySecurity csaTradeable = _tradeablesContainer.csa();

		try
		{
			org.drip.xva.derivative.EvolutionTrajectoryVertex finalTrajectoryVertex = new
				org.drip.xva.derivative.EvolutionTrajectoryVertex (
				initialTime + timeIncrement,
				new org.drip.xva.derivative.ReplicationPortfolioVertex (
					-1. * derivativeXVAValueDeltaFinish,
					gainOnDealerDefaultFinish / dealerSeniorFundingNumeraire,
					!org.drip.quant.common.NumberUtil.IsValid (dealerSubordinateFundingNumeraire) ? 0. :
						gainOnDealerDefaultFinish / dealerSubordinateFundingNumeraire,
					finalClientHoldings,
					initialTrajectoryVertex.replicationPortfolioVertex().cashAccount() +
						cashAccountEdge.accumulation()
				),
				new org.drip.xva.derivative.PositionGreekVertex (
					derivativeXVAValueFinish,
					derivativeXVAValueDeltaFinish,
					initialPositionGreekVertex.derivativeXVAValueGamma() +
						(thetaPositionValueUp + thetaPositionValueDown - 2. * theta) *
						timeIncrement / (positionValueBump * positionValueBump),
					initialPositionGreekVertex.derivativeFairValue() * java.lang.Math.exp (
						-1. * timeIncrement *
						csaTradeable.evolver().evaluator().drift().value (
							new org.drip.measure.realization.JumpDiffusionVertex (
								initialTime - 0.5 * timeIncrement,
								marketEdge.start().csaReplicator(),
								0.,
								false
							)
						)
					)
				),
				gainOnDealerDefaultFinish,
				finalGainOnClientDefault,
				collateral,
				burgardKjaerEdgeRun.derivativeXVAHedgeErrorGrowth()
			);

			return new org.drip.xva.derivative.EvolutionTrajectoryEdge (
				initialTrajectoryVertex,
				finalTrajectoryVertex,
				cashAccountEdge
			);
		}
		catch (java.lang.Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Execute a Sequential Array of Euler Time Step Walks
	 * 
	 * @param marketVertexArray Array of Market Vertexes
	 * @param burgardKjaerOperator The Burgard Kjaer Operator Instance
	 * @param initialTrajectoryVertex The Starting EET Instance
	 * @param collateral The Applicable Collateral
	 * 
	 * @return Array of EvolutionTrajectoryEdge Instances
	 */

	public org.drip.xva.derivative.EvolutionTrajectoryEdge[] eulerWalk (
		final org.drip.exposure.universe.MarketVertex[] marketVertexArray,
		final org.drip.xva.pde.BurgardKjaerOperator burgardKjaerOperator,
		final org.drip.xva.derivative.EvolutionTrajectoryVertex initialTrajectoryVertex,
		final double collateral)
	{
		if (null == marketVertexArray)
		{
			return null;
		}

		int vertexCount = marketVertexArray.length;
		org.drip.xva.derivative.EvolutionTrajectoryVertex trajectoryVertex = initialTrajectoryVertex;
		org.drip.xva.derivative.EvolutionTrajectoryEdge[] evolutionTrajectoryEdgeArray = 1 >= vertexCount ?
			null : new org.drip.xva.derivative.EvolutionTrajectoryEdge[vertexCount - 1];

		if (0 == vertexCount)
		{
			return null;
		}

		for (int i = vertexCount - 2; i >= 0; --i)
		{
			try
			{
				if (null == (evolutionTrajectoryEdgeArray[i] = eulerWalk (
					new org.drip.exposure.universe.MarketEdge (
						marketVertexArray[i],
						marketVertexArray[i + 1]
					),
					burgardKjaerOperator,
					trajectoryVertex,
					collateral)))
				{
					return null;
				}
			}
			catch (java.lang.Exception e)
			{
				e.printStackTrace();
			}

			trajectoryVertex = evolutionTrajectoryEdgeArray[i].vertexFinish();
		}

		return evolutionTrajectoryEdgeArray;
	}
}
