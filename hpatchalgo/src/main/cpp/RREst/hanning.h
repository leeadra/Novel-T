const float WIN[256] = { 0.0001, 0.0006, 0.0013, 0.0024, 0.0037, 0.0054, 0.0073, 0.0095, 0.0121, 0.0149, 0.0180, 0.0214, 0.0250, 0.0290, 0.0332, 0.0378,
					0.0426, 0.0476, 0.0530, 0.0586, 0.0645, 0.0706, 0.0770, 0.0836, 0.0905, 0.0977, 0.1050, 0.1126, 0.1205, 0.1286, 0.1369, 0.1454,
					0.1541, 0.1630, 0.1721, 0.1815, 0.1910, 0.2007, 0.2106, 0.2206, 0.2308, 0.2412, 0.2518, 0.2625, 0.2733, 0.2842, 0.2953, 0.3065,
					0.3179, 0.3293, 0.3408, 0.3525, 0.3642, 0.3760, 0.3879, 0.3998, 0.4118, 0.4239, 0.4360, 0.4481, 0.4603, 0.4725, 0.4847, 0.4969,
					0.5092, 0.5214, 0.5336, 0.5458, 0.5579, 0.5701, 0.5821, 0.5942, 0.6061, 0.6181, 0.6299, 0.6417, 0.6533, 0.6649, 0.6764, 0.6878,
					0.6991, 0.7102, 0.7213, 0.7322, 0.7429, 0.7535, 0.7640, 0.7743, 0.7844, 0.7944, 0.8042, 0.8138, 0.8232, 0.8324, 0.8415, 0.8503,
					0.8589, 0.8673, 0.8755, 0.8835, 0.8912, 0.8987, 0.9059, 0.9130, 0.9197, 0.9262, 0.9325, 0.9385, 0.9442, 0.9497, 0.9549, 0.9599,
					0.9645, 0.9689, 0.9730, 0.9768, 0.9804, 0.9836, 0.9866, 0.9892, 0.9916, 0.9937, 0.9955, 0.9970, 0.9982, 0.9991, 0.9997, 1.0000,
					1.0000, 0.9997, 0.9991, 0.9982, 0.9970, 0.9955, 0.9937, 0.9916, 0.9892, 0.9866, 0.9836, 0.9804, 0.9768, 0.9730, 0.9689, 0.9645,
					0.9599, 0.9549, 0.9497, 0.9442, 0.9385, 0.9325, 0.9262, 0.9197, 0.9130, 0.9059, 0.8987, 0.8912, 0.8835, 0.8755, 0.8673, 0.8589,
					0.8503, 0.8415, 0.8324, 0.8232, 0.8138, 0.8042, 0.7944, 0.7844, 0.7743, 0.7640, 0.7535, 0.7429, 0.7322, 0.7213, 0.7102, 0.6991,
					0.6878, 0.6764, 0.6649, 0.6533, 0.6417, 0.6299, 0.6181, 0.6061, 0.5942, 0.5821, 0.5701, 0.5579, 0.5458, 0.5336, 0.5214, 0.5092,
					0.4969, 0.4847, 0.4725, 0.4603, 0.4481, 0.4360, 0.4239, 0.4118, 0.3998, 0.3879, 0.3760, 0.3642, 0.3525, 0.3408, 0.3293, 0.3179,
					0.3065, 0.2953, 0.2842, 0.2733, 0.2625, 0.2518, 0.2412, 0.2308, 0.2206, 0.2106, 0.2007, 0.1910, 0.1815, 0.1721, 0.1630, 0.1541,
					0.1454, 0.1369, 0.1286, 0.1205, 0.1126, 0.1050, 0.0977, 0.0905, 0.0836, 0.0770, 0.0706, 0.0645, 0.0586, 0.0530, 0.0476, 0.0426,
					0.0378, 0.0332, 0.0290, 0.0250, 0.0214, 0.0180, 0.0149, 0.0121, 0.0095, 0.0073, 0.0054, 0.0037, 0.0024, 0.0013, 0.0006, 0.0001 };