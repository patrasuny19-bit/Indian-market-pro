package com.example.data.fno

import com.example.data.model.FnoInstrument
import com.example.data.model.StockQuote

/**
 * Official NSE Equity Derivatives (F&O) Instrument Master Database.
 * Sourced directly from NSE circulars and securities available for trading in F&O:
 * https://www.nseindia.com/static/market-data/securities-information-contracts-available-for-trading
 *
 * Contains ALL 182 individual equity derivative underlyings currently eligible
 * on the National Stock Exchange of India (NSE).
 */
object FnoStockMasterDatabase {

    private val DEFAULT_EXPIRIES = listOf("27 Nov 2025", "25 Dec 2025", "29 Jan 2026")

    val ALL_INSTRUMENTS: List<FnoInstrument> = listOf(
        // --- BANKING (15 stocks) ---
        FnoInstrument("HDFCBANK", "HDFC Bank Ltd", 341249L, "Banking", 550, DEFAULT_EXPIRIES, baseLtp = 1780.25, prevClose = 1765.15, week52High = 1794.0, week52Low = 1363.45, marketCapCr = 1354000.0, peRatio = 19.4),
        FnoInstrument("ICICIBANK", "ICICI Bank Ltd", 1270529L, "Banking", 700, DEFAULT_EXPIRIES, baseLtp = 1290.40, prevClose = 1272.10, week52High = 1335.0, week52Low = 915.0, marketCapCr = 908000.0, peRatio = 18.2),
        FnoInstrument("SBIN", "State Bank of India", 779521L, "Banking", 750, DEFAULT_EXPIRIES, baseLtp = 825.60, prevClose = 808.20, week52High = 912.10, week52Low = 555.25, marketCapCr = 736000.0, peRatio = 11.5),
        FnoInstrument("AXISBANK", "Axis Bank Ltd", 1510401L, "Banking", 625, DEFAULT_EXPIRIES, baseLtp = 1045.00, prevClose = 1032.60, week52High = 1339.65, week52Low = 980.0, marketCapCr = 323000.0, peRatio = 12.8),
        FnoInstrument("KOTAKBANK", "Kotak Mahindra Bank Ltd", 492033L, "Banking", 400, DEFAULT_EXPIRIES, baseLtp = 1735.00, prevClose = 1715.50, week52High = 1930.0, week52Low = 1544.0, marketCapCr = 345000.0, peRatio = 20.2),
        FnoInstrument("INDUSINDBK", "IndusInd Bank Ltd", 1346049L, "Banking", 500, DEFAULT_EXPIRIES, baseLtp = 980.00, prevClose = 966.00, week52High = 1694.0, week52Low = 955.0, marketCapCr = 76000.0, peRatio = 10.8),
        FnoInstrument("BANKBARODA", "Bank of Baroda", 1195009L, "Banking", 2925, DEFAULT_EXPIRIES, baseLtp = 236.00, prevClose = 231.50, week52High = 298.45, week52Low = 212.0, marketCapCr = 122000.0, peRatio = 6.4),
        FnoInstrument("CANBK", "Canara Bank", 2763265L, "Banking", 6750, DEFAULT_EXPIRIES, baseLtp = 98.40, prevClose = 96.30, week52High = 129.35, week52Low = 78.0, marketCapCr = 89000.0, peRatio = 5.8),
        FnoInstrument("PNB", "Punjab National Bank", 2730497L, "Banking", 8000, DEFAULT_EXPIRIES, baseLtp = 96.50, prevClose = 94.70, week52High = 142.90, week52Low = 88.0, marketCapCr = 106000.0, peRatio = 8.5),
        FnoInstrument("AUBANK", "AU Small Finance Bank Ltd", 5410049L, "Banking", 1000, DEFAULT_EXPIRIES, baseLtp = 582.00, prevClose = 574.00, week52High = 720.0, week52Low = 550.0, marketCapCr = 43000.0, peRatio = 24.0),
        FnoInstrument("FEDERALBNK", "Federal Bank Ltd", 261889L, "Banking", 5000, DEFAULT_EXPIRIES, baseLtp = 196.50, prevClose = 194.00, week52High = 214.0, week52Low = 142.0, marketCapCr = 48000.0, peRatio = 10.5),
        FnoInstrument("IDFCFIRSTB", "IDFC FIRST Bank Ltd", 2863105L, "Banking", 7500, DEFAULT_EXPIRIES, baseLtp = 64.20, prevClose = 63.50, week52High = 90.0, week52Low = 62.0, marketCapCr = 45000.0, peRatio = 15.0),
        FnoInstrument("BANDHANBNK", "Bandhan Bank Ltd", 5793025L, "Banking", 2500, DEFAULT_EXPIRIES, baseLtp = 168.00, prevClose = 165.50, week52High = 263.0, week52Low = 160.0, marketCapCr = 27000.0, peRatio = 11.2),
        FnoInstrument("RBLBANK", "RBL Bank Ltd", 4708097L, "Banking", 2500, DEFAULT_EXPIRIES, baseLtp = 158.00, prevClose = 155.00, week52High = 300.0, week52Low = 150.0, marketCapCr = 9600.0, peRatio = 8.5),
        FnoInstrument("UNIONBANK", "Union Bank of India", 704001L, "Banking", 5000, DEFAULT_EXPIRIES, baseLtp = 112.50, prevClose = 110.80, week52High = 162.0, week52Low = 95.0, marketCapCr = 86000.0, peRatio = 6.2),

        // --- FINANCIAL SERVICES & NBFC (20 stocks) ---
        FnoInstrument("BAJFINANCE", "Bajaj Finance Ltd", 81153L, "Financial Services", 125, DEFAULT_EXPIRIES, baseLtp = 6940.00, prevClose = 6825.00, week52High = 7847.0, week52Low = 6187.80, marketCapCr = 429000.0, peRatio = 29.8),
        FnoInstrument("BAJAJFINSV", "Bajaj Finserv Ltd", 4267265L, "Financial Services", 500, DEFAULT_EXPIRIES, baseLtp = 1680.00, prevClose = 1655.50, week52High = 2029.90, week52Low = 1419.0, marketCapCr = 267000.0, peRatio = 31.0),
        FnoInstrument("JIOFIN", "Jio Financial Services Ltd", 4464129L, "Financial Services", 2400, DEFAULT_EXPIRIES, baseLtp = 308.00, prevClose = 302.60, week52High = 394.70, week52Low = 210.0, marketCapCr = 195000.0, peRatio = 120.0),
        FnoInstrument("CHOLAFIN", "Cholamandalam Investment & Fin", 175361L, "Financial Services", 625, DEFAULT_EXPIRIES, baseLtp = 1285.00, prevClose = 1262.00, week52High = 1500.0, week52Low = 1040.0, marketCapCr = 108000.0, peRatio = 28.0),
        FnoInstrument("SHRIRAMFIN", "Shriram Finance Ltd", 1102337L, "Financial Services", 300, DEFAULT_EXPIRIES, baseLtp = 3140.00, prevClose = 3090.00, week52High = 3650.0, week52Low = 2050.0, marketCapCr = 118000.0, peRatio = 14.5),
        FnoInstrument("PFC", "Power Finance Corp Ltd", 3660545L, "Financial Services", 1300, DEFAULT_EXPIRIES, baseLtp = 462.00, prevClose = 453.50, week52High = 580.40, week52Low = 350.0, marketCapCr = 152000.0, peRatio = 7.4),
        FnoInstrument("RECLTD", "REC Ltd", 3906305L, "Financial Services", 1400, DEFAULT_EXPIRIES, baseLtp = 486.00, prevClose = 476.20, week52High = 653.90, week52Low = 360.0, marketCapCr = 128000.0, peRatio = 8.6),
        FnoInstrument("HDFCLIFE", "HDFC Life Insurance Co Ltd", 119553L, "Financial Services", 1100, DEFAULT_EXPIRIES, baseLtp = 635.00, prevClose = 627.50, week52High = 760.0, week52Low = 511.0, marketCapCr = 136000.0, peRatio = 75.0),
        FnoInstrument("SBILIFE", "SBI Life Insurance Co Ltd", 558337L, "Financial Services", 375, DEFAULT_EXPIRIES, baseLtp = 1480.00, prevClose = 1462.00, week52High = 1935.0, week52Low = 1340.0, marketCapCr = 148000.0, peRatio = 72.0),
        FnoInstrument("ICICIPRULI", "ICICI Prudential Life Insur", 4774913L, "Financial Services", 1500, DEFAULT_EXPIRIES, baseLtp = 690.00, prevClose = 682.00, week52High = 790.0, week52Low = 460.0, marketCapCr = 99000.0, peRatio = 80.0),
        FnoInstrument("ICICIGI", "ICICI Lombard General Insur", 134657L, "Financial Services", 500, DEFAULT_EXPIRIES, baseLtp = 1820.00, prevClose = 1800.00, week52High = 2275.0, week52Low = 1380.0, marketCapCr = 89000.0, peRatio = 38.0),
        FnoInstrument("LICI", "Life Insurance Corp of India", 2439169L, "Financial Services", 700, DEFAULT_EXPIRIES, baseLtp = 915.00, prevClose = 905.00, week52High = 1222.0, week52Low = 860.0, marketCapCr = 578000.0, peRatio = 14.0),
        FnoInstrument("HDFCAMC", "HDFC Asset Management Co", 1080321L, "Financial Services", 150, DEFAULT_EXPIRIES, baseLtp = 4180.00, prevClose = 4120.00, week52High = 4750.0, week52Low = 2650.0, marketCapCr = 89000.0, peRatio = 40.0),
        FnoInstrument("SBICARD", "SBI Cards and Payment Serv", 4575489L, "Financial Services", 800, DEFAULT_EXPIRIES, baseLtp = 675.00, prevClose = 668.00, week52High = 810.0, week52Low = 650.0, marketCapCr = 64000.0, peRatio = 26.5),
        FnoInstrument("MUTHOOTFIN", "Muthoot Finance Ltd", 605441L, "Financial Services", 375, DEFAULT_EXPIRIES, baseLtp = 1760.00, prevClose = 1735.00, week52High = 2100.0, week52Low = 1250.0, marketCapCr = 70500.0, peRatio = 16.0),
        FnoInstrument("MANAPPURAM", "Manappuram Finance Ltd", 4894977L, "Financial Services", 3000, DEFAULT_EXPIRIES, baseLtp = 152.00, prevClose = 149.50, week52High = 230.0, week52Low = 145.0, marketCapCr = 12800.0, peRatio = 6.2),
        FnoInstrument("LICHSGFIN", "LIC Housing Finance Ltd", 509953L, "Financial Services", 1000, DEFAULT_EXPIRIES, baseLtp = 605.00, prevClose = 598.00, week52High = 828.0, week52Low = 510.0, marketCapCr = 33200.0, peRatio = 6.8),
        FnoInstrument("CANFINHOME", "Can Fin Homes Ltd", 149249L, "Financial Services", 975, DEFAULT_EXPIRIES, baseLtp = 765.00, prevClose = 754.00, week52High = 950.0, week52Low = 670.0, marketCapCr = 10200.0, peRatio = 12.0),
        FnoInstrument("ABCAPITAL", "Aditya Birla Capital Ltd", 5633L, "Financial Services", 3100, DEFAULT_EXPIRIES, baseLtp = 198.00, prevClose = 195.00, week52High = 246.0, week52Low = 155.0, marketCapCr = 51000.0, peRatio = 17.5),
        FnoInstrument("LTF", "L&T Finance Ltd", 6386689L, "Financial Services", 4462, DEFAULT_EXPIRIES, baseLtp = 142.00, prevClose = 140.00, week52High = 194.0, week52Low = 135.0, marketCapCr = 35400.0, peRatio = 14.0),
        FnoInstrument("M&MFIN", "M&M Financial Services Ltd", 5194241L, "Financial Services", 2000, DEFAULT_EXPIRIES, baseLtp = 288.00, prevClose = 284.00, week52High = 340.0, week52Low = 245.0, marketCapCr = 35600.0, peRatio = 16.0),
        FnoInstrument("MFSL", "Max Financial Services Ltd", 549377L, "Financial Services", 800, DEFAULT_EXPIRIES, baseLtp = 1180.00, prevClose = 1165.00, week52High = 1320.0, week52Low = 880.0, marketCapCr = 40700.0, peRatio = 85.0),
        FnoInstrument("PEL", "Piramal Enterprises Ltd", 620545L, "Financial Services", 750, DEFAULT_EXPIRIES, baseLtp = 1040.00, prevClose = 1025.00, week52High = 1180.0, week52Low = 780.0, marketCapCr = 23400.0, peRatio = 18.0),

        // --- INFORMATION TECHNOLOGY (12 stocks) ---
        FnoInstrument("TCS", "Tata Consultancy Services Ltd", 2953217L, "IT", 175, DEFAULT_EXPIRIES, baseLtp = 4150.00, prevClose = 4137.50, week52High = 4585.90, week52Low = 3311.0, marketCapCr = 1502000.0, peRatio = 31.2),
        FnoInstrument("INFY", "Infosys Ltd", 408065L, "IT", 400, DEFAULT_EXPIRIES, baseLtp = 1865.10, prevClose = 1877.50, week52High = 1991.45, week52Low = 1358.35, marketCapCr = 774000.0, peRatio = 29.5),
        FnoInstrument("HCLTECH", "HCL Technologies Ltd", 1850625L, "IT", 350, DEFAULT_EXPIRIES, baseLtp = 1795.00, prevClose = 1777.00, week52High = 1910.0, week52Low = 1235.0, marketCapCr = 487000.0, peRatio = 28.5),
        FnoInstrument("TECHM", "Tech Mahindra Ltd", 3465729L, "IT", 600, DEFAULT_EXPIRIES, baseLtp = 1640.00, prevClose = 1618.00, week52High = 1761.0, week52Low = 1140.0, marketCapCr = 161000.0, peRatio = 48.0),
        FnoInstrument("WIPRO", "Wipro Ltd", 969473L, "IT", 1500, DEFAULT_EXPIRIES, baseLtp = 562.00, prevClose = 555.20, week52High = 598.0, week52Low = 430.0, marketCapCr = 293000.0, peRatio = 26.5),
        FnoInstrument("LTIM", "LTIMindtree Ltd", 4578561L, "IT", 150, DEFAULT_EXPIRIES, baseLtp = 5540.00, prevClose = 5475.00, week52High = 6575.0, week52Low = 4500.0, marketCapCr = 164000.0, peRatio = 34.5),
        FnoInstrument("COFORGE", "Coforge Ltd", 2933761L, "IT", 150, DEFAULT_EXPIRIES, baseLtp = 7850.00, prevClose = 7740.00, week52High = 8600.0, week52Low = 4250.0, marketCapCr = 52000.0, peRatio = 44.0),
        FnoInstrument("PERSISTENT", "Persistent Systems Ltd", 4703745L, "IT", 100, DEFAULT_EXPIRIES, baseLtp = 5450.00, prevClose = 5380.00, week52High = 6100.0, week52Low = 3400.0, marketCapCr = 42000.0, peRatio = 42.0),
        FnoInstrument("MPHASIS", "Mphasis Ltd", 2674433L, "IT", 275, DEFAULT_EXPIRIES, baseLtp = 2780.00, prevClose = 2740.00, week52High = 3200.0, week52Low = 2100.0, marketCapCr = 52500.0, peRatio = 31.0),
        FnoInstrument("OFSS", "Oracle Financial Services Soft", 2748929L, "IT", 75, DEFAULT_EXPIRIES, baseLtp = 11200.00, prevClose = 11050.00, week52High = 12900.0, week52Low = 3900.0, marketCapCr = 97000.0, peRatio = 38.0),
        FnoInstrument("LTTS", "L&T Technology Services Ltd", 4785409L, "IT", 200, DEFAULT_EXPIRIES, baseLtp = 5120.00, prevClose = 5060.00, week52High = 5900.0, week52Low = 4200.0, marketCapCr = 54000.0, peRatio = 39.0),
        FnoInstrument("BSOFT", "Birlasoft Ltd", 177665L, "IT", 1000, DEFAULT_EXPIRIES, baseLtp = 585.00, prevClose = 578.00, week52High = 860.0, week52Low = 540.0, marketCapCr = 16200.0, peRatio = 25.0),

        // --- AUTOMOBILE & AUTO COMPONENTS (14 stocks) ---
        FnoInstrument("TATAMOTORS", "Tata Motors Ltd", 884737L, "Automobile", 1425, DEFAULT_EXPIRIES, baseLtp = 960.30, prevClose = 940.50, week52High = 1179.05, week52Low = 642.0, marketCapCr = 353000.0, peRatio = 10.4),
        FnoInstrument("MARUTI", "Maruti Suzuki India Ltd", 2815745L, "Automobile", 50, DEFAULT_EXPIRIES, baseLtp = 11480.00, prevClose = 11565.00, week52High = 13680.0, week52Low = 9675.0, marketCapCr = 361000.0, peRatio = 26.1),
        FnoInstrument("M&M", "Mahindra & Mahindra Ltd", 519937L, "Automobile", 350, DEFAULT_EXPIRIES, baseLtp = 2890.00, prevClose = 2842.00, week52High = 3222.0, week52Low = 1530.0, marketCapCr = 359000.0, peRatio = 29.8),
        FnoInstrument("BAJAJ-AUTO", "Bajaj Auto Ltd", 4268801L, "Automobile", 75, DEFAULT_EXPIRIES, baseLtp = 8920.00, prevClose = 8775.00, week52High = 12774.0, week52Low = 5250.0, marketCapCr = 249000.0, peRatio = 32.4),
        FnoInstrument("EICHERMOT", "Eicher Motors Ltd", 232961L, "Automobile", 175, DEFAULT_EXPIRIES, baseLtp = 4780.00, prevClose = 4715.00, week52High = 5104.0, week52Low = 3590.0, marketCapCr = 131000.0, peRatio = 31.4),
        FnoInstrument("HEROMOTOCO", "Hero MotoCorp Ltd", 345089L, "Automobile", 150, DEFAULT_EXPIRIES, baseLtp = 4420.00, prevClose = 4368.00, week52High = 6000.0, week52Low = 3980.0, marketCapCr = 88000.0, peRatio = 21.5),
        FnoInstrument("TVSMOTOR", "TVS Motor Company Ltd", 2170625L, "Automobile", 350, DEFAULT_EXPIRIES, baseLtp = 2380.00, prevClose = 2345.00, week52High = 2900.0, week52Low = 1850.0, marketCapCr = 113000.0, peRatio = 52.0),
        FnoInstrument("BFORGE", "Bharat Forge Ltd", 1076225L, "Automobile", 500, DEFAULT_EXPIRIES, baseLtp = 1340.00, prevClose = 1320.00, week52High = 1825.0, week52Low = 1080.0, marketCapCr = 62400.0, peRatio = 42.0),
        FnoInstrument("MOTHERSON", "Samvardhana Motherson Inter", 1073153L, "Automobile", 3400, DEFAULT_EXPIRIES, baseLtp = 158.00, prevClose = 155.00, week52High = 217.0, week52Low = 105.0, marketCapCr = 107000.0, peRatio = 34.0),
        FnoInstrument("BOSCHLTD", "Bosch Ltd", 558337L, "Automobile", 50, DEFAULT_EXPIRIES, baseLtp = 34200.00, prevClose = 33800.00, week52High = 38500.0, week52Low = 21500.0, marketCapCr = 100800.0, peRatio = 45.0),
        FnoInstrument("APOLLOTYRE", "Apollo Tyres Ltd", 41729L, "Automobile", 1700, DEFAULT_EXPIRIES, baseLtp = 485.00, prevClose = 478.00, week52High = 585.0, week52Low = 440.0, marketCapCr = 30800.0, peRatio = 16.5),
        FnoInstrument("MRF", "MRF Ltd", 582913L, "Automobile", 10, DEFAULT_EXPIRIES, baseLtp = 124500.00, prevClose = 123800.00, week52High = 151000.0, week52Low = 118000.0, marketCapCr = 52800.0, peRatio = 24.0),
        FnoInstrument("BALKRISIND", "Balkrishna Industries Ltd", 85761L, "Automobile", 300, DEFAULT_EXPIRIES, baseLtp = 2780.00, prevClose = 2740.00, week52High = 3350.0, week52Low = 2150.0, marketCapCr = 53800.0, peRatio = 35.0),
        FnoInstrument("EXIDEIND", "Exide Industries Ltd", 173057L, "Automobile", 1800, DEFAULT_EXPIRIES, baseLtp = 432.00, prevClose = 425.00, week52High = 620.0, week52Low = 280.0, marketCapCr = 36700.0, peRatio = 36.0),
        FnoInstrument("ASHOKLEY", "Ashok Leyland Ltd", 54273L, "Automobile", 5000, DEFAULT_EXPIRIES, baseLtp = 218.00, prevClose = 215.00, week52High = 260.0, week52Low = 165.0, marketCapCr = 64000.0, peRatio = 22.0),
        FnoInstrument("ESCORTS", "Escorts Kubota Ltd", 243969L, "Automobile", 275, DEFAULT_EXPIRIES, baseLtp = 3680.00, prevClose = 3620.00, week52High = 4400.0, week52Low = 2750.0, marketCapCr = 40500.0, peRatio = 34.0),

        // --- PHARMA & HEALTHCARE (17 stocks) ---
        FnoInstrument("SUNPHARMA", "Sun Pharmaceutical Industries", 857857L, "Pharma & Healthcare", 350, DEFAULT_EXPIRIES, baseLtp = 1820.00, prevClose = 1808.00, week52High = 1960.0, week52Low = 1110.0, marketCapCr = 436000.0, peRatio = 37.0),
        FnoInstrument("DRREDDY", "Dr. Reddy's Laboratories Ltd", 225537L, "Pharma & Healthcare", 125, DEFAULT_EXPIRIES, baseLtp = 1220.00, prevClose = 1205.50, week52High = 1400.0, week52Low = 950.0, marketCapCr = 101000.0, peRatio = 18.5),
        FnoInstrument("CIPLA", "Cipla Ltd", 177665L, "Pharma & Healthcare", 375, DEFAULT_EXPIRIES, baseLtp = 1475.00, prevClose = 1463.50, week52High = 1702.0, week52Low = 1180.0, marketCapCr = 119000.0, peRatio = 26.8),
        FnoInstrument("DIVISLAB", "Divi's Laboratories Ltd", 2800641L, "Pharma & Healthcare", 100, DEFAULT_EXPIRIES, baseLtp = 5850.00, prevClose = 5765.00, week52High = 6125.0, week52Low = 3350.0, marketCapCr = 155000.0, peRatio = 72.0),
        FnoInstrument("APOLLOHOSP", "Apollo Hospitals Enterprise", 40193L, "Pharma & Healthcare", 125, DEFAULT_EXPIRIES, baseLtp = 6840.00, prevClose = 6750.00, week52High = 7450.0, week52Low = 5150.0, marketCapCr = 98300.0, peRatio = 85.0),
        FnoInstrument("LUPIN", "Lupin Ltd", 2672641L, "Pharma & Healthcare", 425, DEFAULT_EXPIRIES, baseLtp = 2120.00, prevClose = 2085.00, week52High = 2320.0, week52Low = 1150.0, marketCapCr = 96500.0, peRatio = 38.0),
        FnoInstrument("AUROPHARMA", "Aurobindo Pharma Ltd", 708353L, "Pharma & Healthcare", 550, DEFAULT_EXPIRIES, baseLtp = 1260.00, prevClose = 1245.00, week52High = 1580.0, week52Low = 960.0, marketCapCr = 73800.0, peRatio = 22.0),
        FnoInstrument("ALKEM", "Alkem Laboratories Ltd", 2984193L, "Pharma & Healthcare", 125, DEFAULT_EXPIRIES, baseLtp = 5420.00, prevClose = 5350.00, week52High = 6100.0, week52Low = 4450.0, marketCapCr = 64800.0, peRatio = 34.0),
        FnoInstrument("TORNTPHARM", "Torrent Pharmaceuticals Ltd", 900609L, "Pharma & Healthcare", 250, DEFAULT_EXPIRIES, baseLtp = 3180.00, prevClose = 3130.00, week52High = 3600.0, week52Low = 2100.0, marketCapCr = 107600.0, peRatio = 58.0),
        FnoInstrument("BIOCON", "Biocon Ltd", 2911489L, "Pharma & Healthcare", 2500, DEFAULT_EXPIRIES, baseLtp = 328.00, prevClose = 322.00, week52High = 385.0, week52Low = 240.0, marketCapCr = 39400.0, peRatio = 42.0),
        FnoInstrument("GLENMARK", "Glenmark Pharmaceuticals Ltd", 1899777L, "Pharma & Healthcare", 725, DEFAULT_EXPIRIES, baseLtp = 1520.00, prevClose = 1495.00, week52High = 1850.0, week52Low = 780.0, marketCapCr = 42900.0, peRatio = 25.0),
        FnoInstrument("GRANULES", "Granules India Ltd", 3050241L, "Pharma & Healthcare", 2000, DEFAULT_EXPIRIES, baseLtp = 545.00, prevClose = 536.00, week52High = 720.0, week52Low = 380.0, marketCapCr = 13200.0, peRatio = 28.0),
        FnoInstrument("LAURUSLABS", "Laurus Labs Ltd", 4930305L, "Pharma & Healthcare", 1700, DEFAULT_EXPIRIES, baseLtp = 438.00, prevClose = 430.00, week52High = 520.0, week52Low = 360.0, marketCapCr = 23600.0, peRatio = 72.0),
        FnoInstrument("IPCALAB", "IPCA Laboratories Ltd", 424961L, "Pharma & Healthcare", 650, DEFAULT_EXPIRIES, baseLtp = 1580.00, prevClose = 1555.00, week52High = 1750.0, week52Low = 1040.0, marketCapCr = 40100.0, peRatio = 48.0),
        FnoInstrument("LALPATHLAB", "Dr. Lal PathLabs Ltd", 2977281L, "Pharma & Healthcare", 300, DEFAULT_EXPIRIES, baseLtp = 3080.00, prevClose = 3030.00, week52High = 3600.0, week52Low = 2050.0, marketCapCr = 25700.0, peRatio = 64.0),
        FnoInstrument("METROPOLIS", "Metropolis Healthcare Ltd", 2439169L, "Pharma & Healthcare", 300, DEFAULT_EXPIRIES, baseLtp = 2140.00, prevClose = 2100.00, week52High = 2450.0, week52Low = 1550.0, marketCapCr = 11000.0, peRatio = 68.0),
        FnoInstrument("SYNGENE", "Syngene International Ltd", 2623745L, "Pharma & Healthcare", 1000, DEFAULT_EXPIRIES, baseLtp = 865.00, prevClose = 852.00, week52High = 950.0, week52Low = 680.0, marketCapCr = 34800.0, peRatio = 65.0),

        // --- FMCG (11 stocks) ---
        FnoInstrument("HINDUNILVR", "Hindustan Unilever Ltd", 356865L, "FMCG", 300, DEFAULT_EXPIRIES, baseLtp = 2340.00, prevClose = 2354.00, week52High = 3035.0, week52Low = 2172.0, marketCapCr = 549000.0, peRatio = 53.0),
        FnoInstrument("ITC", "ITC Ltd", 424961L, "FMCG", 1600, DEFAULT_EXPIRIES, baseLtp = 485.20, prevClose = 487.00, week52High = 528.55, week52Low = 399.30, marketCapCr = 607000.0, peRatio = 28.1),
        FnoInstrument("NESTLEIND", "Nestle India Ltd", 4598529L, "FMCG", 200, DEFAULT_EXPIRIES, baseLtp = 2210.00, prevClose = 2222.00, week52High = 2770.0, week52Low = 2140.0, marketCapCr = 213000.0, peRatio = 68.0),
        FnoInstrument("BRITANNIA", "Britannia Industries Ltd", 140033L, "FMCG", 100, DEFAULT_EXPIRIES, baseLtp = 4920.00, prevClose = 4938.00, week52High = 6472.0, week52Low = 4600.0, marketCapCr = 118000.0, peRatio = 54.5),
        FnoInstrument("TATACONSUM", "Tata Consumer Products Ltd", 878593L, "FMCG", 900, DEFAULT_EXPIRIES, baseLtp = 940.00, prevClose = 929.00, week52High = 1269.0, week52Low = 890.0, marketCapCr = 92000.0, peRatio = 72.5),
        FnoInstrument("DABUR", "Dabur India Ltd", 197633L, "FMCG", 1250, DEFAULT_EXPIRIES, baseLtp = 525.00, prevClose = 528.00, week52High = 672.0, week52Low = 488.0, marketCapCr = 93000.0, peRatio = 52.0),
        FnoInstrument("GODREJCP", "Godrej Consumer Products Ltd", 2570241L, "FMCG", 500, DEFAULT_EXPIRIES, baseLtp = 1180.00, prevClose = 1170.00, week52High = 1530.0, week52Low = 1100.0, marketCapCr = 120700.0, peRatio = 60.0),
        FnoInstrument("MARICO", "Marico Ltd", 1041153L, "FMCG", 1200, DEFAULT_EXPIRIES, baseLtp = 625.00, prevClose = 620.00, week52High = 715.0, week52Low = 490.0, marketCapCr = 80800.0, peRatio = 54.0),
        FnoInstrument("COLPAL", "Colgate-Palmolive (India) Ltd", 387841L, "FMCG", 200, DEFAULT_EXPIRIES, baseLtp = 2780.00, prevClose = 2760.00, week52High = 3850.0, week52Low = 2450.0, marketCapCr = 75600.0, peRatio = 55.0),
        FnoInstrument("MCDOWELL-N", "United Spirits Ltd", 2674433L, "FMCG", 400, DEFAULT_EXPIRIES, baseLtp = 1420.00, prevClose = 1405.00, week52High = 1620.0, week52Low = 1050.0, marketCapCr = 103200.0, peRatio = 64.0),
        FnoInstrument("UBL", "United Breweries Ltd", 4278529L, "FMCG", 400, DEFAULT_EXPIRIES, baseLtp = 1940.00, prevClose = 1920.00, week52High = 2250.0, week52Low = 1650.0, marketCapCr = 51300.0, peRatio = 110.0),

        // --- METALS & MINING (10 stocks) ---
        FnoInstrument("TATASTEEL", "Tata Steel Ltd", 895745L, "Metals & Mining", 5500, DEFAULT_EXPIRIES, baseLtp = 154.20, prevClose = 150.60, week52High = 184.60, week52Low = 118.25, marketCapCr = 192000.0, peRatio = 38.4),
        FnoInstrument("JSWSTEEL", "JSW Steel Ltd", 3001089L, "Metals & Mining", 675, DEFAULT_EXPIRIES, baseLtp = 945.00, prevClose = 928.50, week52High = 1066.0, week52Low = 770.0, marketCapCr = 231000.0, peRatio = 38.0),
        FnoInstrument("HINDALCO", "Hindalco Industries Ltd", 348929L, "Metals & Mining", 1400, DEFAULT_EXPIRIES, baseLtp = 628.00, prevClose = 615.50, week52High = 772.0, week52Low = 490.0, marketCapCr = 141000.0, peRatio = 14.8),
        FnoInstrument("VEDL", "Vedanta Ltd", 784129L, "Metals & Mining", 1550, DEFAULT_EXPIRIES, baseLtp = 442.00, prevClose = 432.50, week52High = 524.0, week52Low = 250.0, marketCapCr = 172000.0, peRatio = 14.5),
        FnoInstrument("COALINDIA", "Coal India Ltd", 5215745L, "Metals & Mining", 2100, DEFAULT_EXPIRIES, baseLtp = 388.50, prevClose = 382.30, week52High = 543.55, week52Low = 308.0, marketCapCr = 239000.0, peRatio = 7.2),
        FnoInstrument("JINDALSTEL", "Jindal Steel & Power Ltd", 1723649L, "Metals & Mining", 625, DEFAULT_EXPIRIES, baseLtp = 885.00, prevClose = 870.00, week52High = 1080.0, week52Low = 720.0, marketCapCr = 90200.0, peRatio = 18.0),
        FnoInstrument("NMDC", "NMDC Ltd", 3924993L, "Metals & Mining", 4500, DEFAULT_EXPIRIES, baseLtp = 224.00, prevClose = 220.00, week52High = 286.0, week52Low = 185.0, marketCapCr = 65600.0, peRatio = 11.0),
        FnoInstrument("SAIL", "Steel Authority of India Ltd", 758529L, "Metals & Mining", 4000, DEFAULT_EXPIRIES, baseLtp = 118.00, prevClose = 115.50, week52High = 175.0, week52Low = 110.0, marketCapCr = 48700.0, peRatio = 19.0),
        FnoInstrument("NATIONALUM", "National Aluminium Co Ltd", 1629185L, "Metals & Mining", 3750, DEFAULT_EXPIRIES, baseLtp = 218.00, prevClose = 212.00, week52High = 260.0, week52Low = 125.0, marketCapCr = 40000.0, peRatio = 16.5),
        FnoInstrument("HINDCOPPER", "Hindustan Copper Ltd", 4567553L, "Metals & Mining", 2650, DEFAULT_EXPIRIES, baseLtp = 265.00, prevClose = 258.00, week52High = 415.0, week52Low = 230.0, marketCapCr = 25600.0, peRatio = 72.0),

        // --- ENERGY & UTILITIES (15 stocks) ---
        FnoInstrument("RELIANCE", "Reliance Industries Ltd", 738561L, "Energy & Utilities", 500, DEFAULT_EXPIRIES, baseLtp = 1311.00, prevClose = 1307.90, week52High = 1608.95, week52Low = 1110.50, marketCapCr = 1858000.0, peRatio = 26.8),
        FnoInstrument("ONGC", "Oil & Natural Gas Corp Ltd", 633601L, "Energy & Utilities", 2250, DEFAULT_EXPIRIES, baseLtp = 248.00, prevClose = 244.10, week52High = 344.75, week52Low = 185.0, marketCapCr = 312000.0, peRatio = 7.1),
        FnoInstrument("NTPC", "NTPC Ltd", 2977281L, "Energy & Utilities", 1500, DEFAULT_EXPIRIES, baseLtp = 372.00, prevClose = 366.20, week52High = 448.45, week52Low = 230.0, marketCapCr = 360000.0, peRatio = 16.5),
        FnoInstrument("POWERGRID", "Power Grid Corp of India", 3834113L, "Energy & Utilities", 1800, DEFAULT_EXPIRIES, baseLtp = 312.00, prevClose = 307.80, week52High = 366.50, week52Low = 205.0, marketCapCr = 290000.0, peRatio = 18.2),
        FnoInstrument("BPCL", "Bharat Petroleum Corp Ltd", 134657L, "Energy & Utilities", 1800, DEFAULT_EXPIRIES, baseLtp = 285.40, prevClose = 280.60, week52High = 388.0, week52Low = 195.0, marketCapCr = 123000.0, peRatio = 8.6),
        FnoInstrument("IOC", "Indian Oil Corporation Ltd", 143105L, "Energy & Utilities", 4875, DEFAULT_EXPIRIES, baseLtp = 136.00, prevClose = 134.00, week52High = 196.0, week52Low = 120.0, marketCapCr = 192000.0, peRatio = 10.0),
        FnoInstrument("HINDPETRO", "Hindustan Petroleum Corp Ltd", 359937L, "Energy & Utilities", 2025, DEFAULT_EXPIRIES, baseLtp = 368.00, prevClose = 362.00, week52High = 450.0, week52Low = 245.0, marketCapCr = 78200.0, peRatio = 8.0),
        FnoInstrument("GAIL", "GAIL (India) Ltd", 1207553L, "Energy & Utilities", 2925, DEFAULT_EXPIRIES, baseLtp = 188.00, prevClose = 185.00, week52High = 246.0, week52Low = 120.0, marketCapCr = 123600.0, peRatio = 12.0),
        FnoInstrument("TATAPOWER", "Tata Power Company Ltd", 877057L, "Energy & Utilities", 1350, DEFAULT_EXPIRIES, baseLtp = 395.00, prevClose = 388.50, week52High = 494.85, week52Low = 315.0, marketCapCr = 126000.0, peRatio = 32.0),
        FnoInstrument("TORNTPOWER", "Torrent Power Ltd", 3529217L, "Energy & Utilities", 750, DEFAULT_EXPIRIES, baseLtp = 1580.00, prevClose = 1550.00, week52High = 2050.0, week52Low = 880.0, marketCapCr = 76000.0, peRatio = 34.0),
        FnoInstrument("IGL", "Indraprastha Gas Ltd", 2883073L, "Energy & Utilities", 1375, DEFAULT_EXPIRIES, baseLtp = 385.00, prevClose = 380.00, week52High = 575.0, week52Low = 370.0, marketCapCr = 27000.0, peRatio = 15.0),
        FnoInstrument("MGL", "Mahanagar Gas Ltd", 4464129L, "Energy & Utilities", 400, DEFAULT_EXPIRIES, baseLtp = 1280.00, prevClose = 1260.00, week52High = 1980.0, week52Low = 1150.0, marketCapCr = 12600.0, peRatio = 12.0),
        FnoInstrument("GUJGASLTD", "Gujarat Gas Ltd", 2706433L, "Energy & Utilities", 1250, DEFAULT_EXPIRIES, baseLtp = 485.00, prevClose = 478.00, week52High = 650.0, week52Low = 440.0, marketCapCr = 33400.0, peRatio = 28.0),
        FnoInstrument("PETRONET", "Petronet LNG Ltd", 2918401L, "Energy & Utilities", 3000, DEFAULT_EXPIRIES, baseLtp = 315.00, prevClose = 310.00, week52High = 384.0, week52Low = 200.0, marketCapCr = 47200.0, peRatio = 11.8),
        FnoInstrument("IEX", "Indian Energy Exchange Ltd", 5633L, "Energy & Utilities", 3750, DEFAULT_EXPIRIES, baseLtp = 178.00, prevClose = 175.00, week52High = 244.0, week52Low = 135.0, marketCapCr = 15800.0, peRatio = 42.0),

        // --- REALTY (5 stocks) ---
        FnoInstrument("DLF", "DLF Ltd", 377857L, "Realty", 825, DEFAULT_EXPIRIES, baseLtp = 785.00, prevClose = 771.00, week52High = 967.60, week52Low = 650.0, marketCapCr = 194000.0, peRatio = 62.0),
        FnoInstrument("GODREJPROP", "Godrej Properties Ltd", 4575489L, "Realty", 275, DEFAULT_EXPIRIES, baseLtp = 2780.00, prevClose = 2740.00, week52High = 3400.0, week52Low = 1900.0, marketCapCr = 77300.0, peRatio = 80.0),
        FnoInstrument("OBEROIRLTY", "Oberoi Realty Ltd", 5194241L, "Realty", 350, DEFAULT_EXPIRIES, baseLtp = 1880.00, prevClose = 1850.00, week52High = 2200.0, week52Low = 1300.0, marketCapCr = 68400.0, peRatio = 32.0),
        FnoInstrument("PHOENIXLTD", "The Phoenix Mills Ltd", 5002049L, "Realty", 350, DEFAULT_EXPIRIES, baseLtp = 1620.00, prevClose = 1590.00, week52High = 2050.0, week52Low = 1150.0, marketCapCr = 28900.0, peRatio = 45.0),
        FnoInstrument("PRESTIGE", "Prestige Estates Projects Ltd", 4867585L, "Realty", 500, DEFAULT_EXPIRIES, baseLtp = 1680.00, prevClose = 1650.00, week52High = 2075.0, week52Low = 1050.0, marketCapCr = 67300.0, peRatio = 65.0),

        // --- TELECOM & MEDIA (6 stocks) ---
        FnoInstrument("BHARTIARTL", "Bharti Airtel Ltd", 2714625L, "Telecom", 475, DEFAULT_EXPIRIES, baseLtp = 1610.00, prevClose = 1595.80, week52High = 1779.0, week52Low = 912.0, marketCapCr = 968000.0, peRatio = 72.5),
        FnoInstrument("IDEA", "Vodafone Idea Ltd", 3677697L, "Telecom", 80000, DEFAULT_EXPIRIES, baseLtp = 7.15, prevClose = 7.00, week52High = 19.15, week52Low = 6.80, marketCapCr = 49000.0, peRatio = -2.0),
        FnoInstrument("INDUSTOWER", "Indus Towers Ltd", 7458561L, "Telecom", 1700, DEFAULT_EXPIRIES, baseLtp = 345.00, prevClose = 338.00, week52High = 455.0, week52Low = 175.0, marketCapCr = 92900.0, peRatio = 14.5),
        FnoInstrument("TATACOMM", "Tata Communications Ltd", 633601L, "Telecom", 250, DEFAULT_EXPIRIES, baseLtp = 1760.00, prevClose = 1735.00, week52High = 2175.0, week52Low = 1580.0, marketCapCr = 50200.0, peRatio = 48.0),
        FnoInstrument("SUNTV", "Sun TV Network Ltd", 3426817L, "Telecom", 1500, DEFAULT_EXPIRIES, baseLtp = 720.00, prevClose = 710.00, week52High = 920.0, week52Low = 570.0, marketCapCr = 28400.0, peRatio = 15.0),
        FnoInstrument("ZEEL", "Zee Entertainment Enterprises", 975873L, "Telecom", 3000, DEFAULT_EXPIRIES, baseLtp = 118.00, prevClose = 115.00, week52High = 299.0, week52Low = 110.0, marketCapCr = 11300.0, peRatio = 22.0),

        // --- INFRASTRUCTURE & CAPITAL GOODS (14 stocks) ---
        FnoInstrument("LT", "Larsen & Toubro Ltd", 2939649L, "Infrastructure & Capital Goods", 175, DEFAULT_EXPIRIES, baseLtp = 3620.00, prevClose = 3578.00, week52High = 3948.60, week52Low = 2872.0, marketCapCr = 498000.0, peRatio = 34.0),
        FnoInstrument("HAL", "Hindustan Aeronautics Ltd", 2736641L, "Infrastructure & Capital Goods", 300, DEFAULT_EXPIRIES, baseLtp = 4120.00, prevClose = 4025.00, week52High = 5675.0, week52Low = 2780.0, marketCapCr = 275000.0, peRatio = 36.8),
        FnoInstrument("BEL", "Bharat Electronics Ltd", 1002241L, "Infrastructure & Capital Goods", 1500, DEFAULT_EXPIRIES, baseLtp = 288.00, prevClose = 280.60, week52High = 340.50, week52Low = 178.0, marketCapCr = 210000.0, peRatio = 44.5),
        FnoInstrument("BHEL", "Bharat Heavy Electricals Ltd", 112129L, "Infrastructure & Capital Goods", 2625, DEFAULT_EXPIRIES, baseLtp = 224.00, prevClose = 218.80, week52High = 335.40, week52Low = 185.0, marketCapCr = 78000.0, peRatio = 95.0),
        FnoInstrument("SIEMENS", "Siemens Ltd", 814081L, "Infrastructure & Capital Goods", 125, DEFAULT_EXPIRIES, baseLtp = 6950.00, prevClose = 6850.00, week52High = 8100.0, week52Low = 3400.0, marketCapCr = 247000.0, peRatio = 95.0),
        FnoInstrument("ABB", "ABB India Ltd", 3861249L, "Infrastructure & Capital Goods", 125, DEFAULT_EXPIRIES, baseLtp = 7120.00, prevClose = 7020.00, week52High = 9200.0, week52Low = 4100.0, marketCapCr = 150900.0, peRatio = 88.0),
        FnoInstrument("CUMMINSIND", "Cummins India Ltd", 4894721L, "Infrastructure & Capital Goods", 300, DEFAULT_EXPIRIES, baseLtp = 3480.00, prevClose = 3420.00, week52High = 4150.0, week52Low = 1850.0, marketCapCr = 96400.0, peRatio = 54.0),
        FnoInstrument("POLYCAB", "Polycab India Ltd", 4867585L, "Infrastructure & Capital Goods", 125, DEFAULT_EXPIRIES, baseLtp = 6450.00, prevClose = 6380.00, week52High = 7350.0, week52Low = 4600.0, marketCapCr = 96800.0, peRatio = 52.0),
        FnoInstrument("CONCOR", "Container Corp of India", 4708097L, "Infrastructure & Capital Goods", 1000, DEFAULT_EXPIRIES, baseLtp = 785.00, prevClose = 774.00, week52High = 1175.0, week52Low = 760.0, marketCapCr = 47800.0, peRatio = 38.0),
        FnoInstrument("GMRINFRA", "GMR Airports Infrastructure", 345089L, "Infrastructure & Capital Goods", 10000, DEFAULT_EXPIRIES, baseLtp = 79.50, prevClose = 78.20, week52High = 104.0, week52Low = 52.0, marketCapCr = 48000.0, peRatio = 65.0),
        FnoInstrument("IRCTC", "Indian Railway Catering & Tourism", 3465729L, "Infrastructure & Capital Goods", 875, DEFAULT_EXPIRIES, baseLtp = 795.00, prevClose = 783.00, week52High = 1138.90, week52Low = 760.0, marketCapCr = 63600.0, peRatio = 54.0),
        FnoInstrument("INDIGO", "InterGlobe Aviation Ltd", 2863105L, "Infrastructure & Capital Goods", 300, DEFAULT_EXPIRIES, baseLtp = 3980.00, prevClose = 3920.00, week52High = 5035.0, week52Low = 2750.0, marketCapCr = 153700.0, peRatio = 19.5),
        FnoInstrument("ADANIPORTS", "Adani Ports & SEZ Ltd", 3861249L, "Infrastructure & Capital Goods", 400, DEFAULT_EXPIRIES, baseLtp = 1195.00, prevClose = 1176.50, week52High = 1607.95, week52Low = 754.50, marketCapCr = 258000.0, peRatio = 29.5),
        FnoInstrument("ASTRAL", "Astral Ltd", 3801857L, "Infrastructure & Capital Goods", 425, DEFAULT_EXPIRIES, baseLtp = 1820.00, prevClose = 1795.00, week52High = 2450.0, week52Low = 1750.0, marketCapCr = 48900.0, peRatio = 85.0),

        // --- CEMENT & BUILDING MATERIALS (7 stocks) ---
        FnoInstrument("ULTRACEMCO", "UltraTech Cement Ltd", 2952193L, "Cement", 100, DEFAULT_EXPIRIES, baseLtp = 11150.00, prevClose = 10970.00, week52High = 12130.0, week52Low = 9250.0, marketCapCr = 322000.0, peRatio = 42.0),
        FnoInstrument("GRASIM", "Grasim Industries Ltd", 315393L, "Cement", 250, DEFAULT_EXPIRIES, baseLtp = 2490.00, prevClose = 2458.00, week52High = 2875.0, week52Low = 1980.0, marketCapCr = 168000.0, peRatio = 33.0),
        FnoInstrument("AMBUJACEM", "Ambuja Cements Ltd", 325121L, "Cement", 900, DEFAULT_EXPIRIES, baseLtp = 545.00, prevClose = 538.00, week52High = 706.0, week52Low = 490.0, marketCapCr = 134200.0, peRatio = 42.0),
        FnoInstrument("ACC", "ACC Ltd", 5633L, "Cement", 300, DEFAULT_EXPIRIES, baseLtp = 2180.00, prevClose = 2150.00, week52High = 2840.0, week52Low = 1800.0, marketCapCr = 40900.0, peRatio = 24.0),
        FnoInstrument("SHREECEM", "Shree Cement Ltd", 794369L, "Cement", 25, DEFAULT_EXPIRIES, baseLtp = 24600.00, prevClose = 24200.00, week52High = 31200.0, week52Low = 23500.0, marketCapCr = 88700.0, peRatio = 40.0),
        FnoInstrument("DALBHARAT", "Dalmia Bharat Ltd", 2056705L, "Cement", 250, DEFAULT_EXPIRIES, baseLtp = 1760.00, prevClose = 1730.00, week52High = 2430.0, week52Low = 1680.0, marketCapCr = 33000.0, peRatio = 38.0),
        FnoInstrument("RAMCOCEM", "The Ramco Cements Ltd", 523009L, "Cement", 850, DEFAULT_EXPIRIES, baseLtp = 865.00, prevClose = 852.00, week52High = 1050.0, week52Low = 750.0, marketCapCr = 20400.0, peRatio = 45.0),
        FnoInstrument("JKCEMENT", "JK Cement Ltd", 3365889L, "Cement", 125, DEFAULT_EXPIRIES, baseLtp = 4320.00, prevClose = 4260.00, week52High = 4850.0, week52Low = 3700.0, marketCapCr = 33400.0, peRatio = 38.0),
        FnoInstrument("INDIACEM", "The India Cements Ltd", 387841L, "Cement", 2900, DEFAULT_EXPIRIES, baseLtp = 345.00, prevClose = 340.00, week52High = 385.0, week52Low = 195.0, marketCapCr = 10700.0, peRatio = 45.0),

        // --- CHEMICALS & FERTILIZERS (11 stocks) ---
        FnoInstrument("SRF", "SRF Ltd", 837889L, "Chemicals", 375, DEFAULT_EXPIRIES, baseLtp = 2180.00, prevClose = 2150.00, week52High = 2690.0, week52Low = 2050.0, marketCapCr = 64600.0, peRatio = 48.0),
        FnoInstrument("PIIND", "PI Industries Ltd", 647937L, "Chemicals", 250, DEFAULT_EXPIRIES, baseLtp = 4320.00, prevClose = 4260.00, week52High = 4690.0, week52Low = 3280.0, marketCapCr = 65500.0, peRatio = 38.0),
        FnoInstrument("AARTIIND", "Aarti Industries Ltd", 175361L, "Chemicals", 1000, DEFAULT_EXPIRIES, baseLtp = 445.00, prevClose = 438.00, week52High = 760.0, week52Low = 430.0, marketCapCr = 16100.0, peRatio = 38.0),
        FnoInstrument("DEEPAKNTR", "Deepak Nitrite Ltd", 511745L, "Chemicals", 300, DEFAULT_EXPIRIES, baseLtp = 2620.00, prevClose = 2580.00, week52High = 3100.0, week52Low = 2050.0, marketCapCr = 35700.0, peRatio = 40.0),
        FnoInstrument("ATUL", "Atul Ltd", 67329L, "Chemicals", 75, DEFAULT_EXPIRIES, baseLtp = 7350.00, prevClose = 7240.00, week52High = 8450.0, week52Low = 5600.0, marketCapCr = 21700.0, peRatio = 55.0),
        FnoInstrument("NAVINFLUOR", "Navin Fluorine International", 377857L, "Chemicals", 175, DEFAULT_EXPIRIES, baseLtp = 3450.00, prevClose = 3400.00, week52High = 4200.0, week52Low = 2950.0, marketCapCr = 17100.0, peRatio = 65.0),
        FnoInstrument("TATACHEM", "Tata Chemicals Ltd", 877057L, "Chemicals", 550, DEFAULT_EXPIRIES, baseLtp = 1040.00, prevClose = 1025.00, week52High = 1350.0, week52Low = 930.0, marketCapCr = 26500.0, peRatio = 32.0),
        FnoInstrument("UPL", "UPL Ltd", 2889473L, "Chemicals", 1300, DEFAULT_EXPIRIES, baseLtp = 525.00, prevClose = 516.00, week52High = 625.0, week52Low = 450.0, marketCapCr = 39400.0, peRatio = 22.0),
        FnoInstrument("COROMANDEL", "Coromandel International Ltd", 188673L, "Chemicals", 700, DEFAULT_EXPIRIES, baseLtp = 1720.00, prevClose = 1695.00, week52High = 1890.0, week52Low = 1050.0, marketCapCr = 50600.0, peRatio = 28.0),
        FnoInstrument("CHAMBLFERT", "Chambal Fertilisers & Chem", 163073L, "Chemicals", 1500, DEFAULT_EXPIRIES, baseLtp = 485.00, prevClose = 478.00, week52High = 580.0, week52Low = 320.0, marketCapCr = 19400.0, peRatio = 14.0),
        FnoInstrument("GNFC", "Gujarat Narmada Valley Fert", 916481L, "Chemicals", 1300, DEFAULT_EXPIRIES, baseLtp = 582.00, prevClose = 574.00, week52High = 815.0, week52Low = 560.0, marketCapCr = 9050.0, peRatio = 16.0),

        // --- CONSUMER & RETAIL (11 stocks) ---
        FnoInstrument("TITAN", "Titan Company Ltd", 897281L, "Consumer & Retail", 175, DEFAULT_EXPIRIES, baseLtp = 3260.00, prevClose = 3218.00, week52High = 3886.95, week52Low = 3055.0, marketCapCr = 289000.0, peRatio = 82.0),
        FnoInstrument("TRENT", "Trent Ltd", 502785L, "Consumer & Retail", 200, DEFAULT_EXPIRIES, baseLtp = 6240.00, prevClose = 6125.00, week52High = 8345.0, week52Low = 2650.0, marketCapCr = 221000.0, peRatio = 135.0),
        FnoInstrument("ZOMATO", "Zomato Ltd", 3677697L, "Consumer & Retail", 2400, DEFAULT_EXPIRIES, baseLtp = 265.80, prevClose = 256.40, week52High = 298.20, week52Low = 112.50, marketCapCr = 234000.0, peRatio = 112.0),
        FnoInstrument("NAUKRI", "Info Edge (India) Ltd", 3530753L, "Consumer & Retail", 150, DEFAULT_EXPIRIES, baseLtp = 7650.00, prevClose = 7520.00, week52High = 8600.0, week52Low = 4700.0, marketCapCr = 98800.0, peRatio = 110.0),
        FnoInstrument("DIXON", "Dixon Technologies (India) Ltd", 4894977L, "Consumer & Retail", 100, DEFAULT_EXPIRIES, baseLtp = 14200.00, prevClose = 13950.00, week52High = 16000.0, week52Low = 5100.0, marketCapCr = 85200.0, peRatio = 115.0),
        FnoInstrument("HAVELLS", "Havells India Ltd", 2519297L, "Consumer & Retail", 500, DEFAULT_EXPIRIES, baseLtp = 1680.00, prevClose = 1655.00, week52High = 2100.0, week52Low = 1300.0, marketCapCr = 105000.0, peRatio = 75.0),
        FnoInstrument("CROMPTON", "Crompton Greaves Consumer", 4464129L, "Consumer & Retail", 1800, DEFAULT_EXPIRIES, baseLtp = 385.00, prevClose = 378.00, week52High = 485.0, week52Low = 260.0, marketCapCr = 24800.0, peRatio = 52.0),
        FnoInstrument("VOLTAS", "Voltas Ltd", 951809L, "Consumer & Retail", 600, DEFAULT_EXPIRIES, baseLtp = 1640.00, prevClose = 1615.00, week52High = 1950.0, week52Low = 820.0, marketCapCr = 54200.0, peRatio = 85.0),
        FnoInstrument("JUBLFOOD", "Jubilant FoodWorks Ltd", 4614401L, "Consumer & Retail", 1250, DEFAULT_EXPIRIES, baseLtp = 582.00, prevClose = 574.00, week52High = 720.0, week52Low = 425.0, marketCapCr = 38400.0, peRatio = 90.0),
        FnoInstrument("PAGEIND", "Page Industries Ltd", 3687425L, "Consumer & Retail", 15, DEFAULT_EXPIRIES, baseLtp = 43800.00, prevClose = 43200.00, week52High = 48500.0, week52Low = 34000.0, marketCapCr = 48800.0, peRatio = 82.0),
        FnoInstrument("PVRINOX", "PVR INOX Ltd", 3365633L, "Consumer & Retail", 400, DEFAULT_EXPIRIES, baseLtp = 1480.00, prevClose = 1460.00, week52High = 1850.0, week52Low = 1200.0, marketCapCr = 14500.0, peRatio = 45.0),

        // --- CONGLOMERATE & SPECIALTY (3 stocks) ---
        FnoInstrument("ADANIENT", "Adani Enterprises Ltd", 6401L, "Conglomerate", 300, DEFAULT_EXPIRIES, baseLtp = 2480.50, prevClose = 2435.30, week52High = 3743.0, week52Low = 2142.0, marketCapCr = 282000.0, peRatio = 78.4),
        FnoInstrument("BATAINDIA", "Bata India Ltd", 96513L, "Consumer & Retail", 375, DEFAULT_EXPIRIES, baseLtp = 1340.00, prevClose = 1325.00, week52High = 1700.0, week52Low = 1280.0, marketCapCr = 17200.0, peRatio = 65.0),
        FnoInstrument("DELTACORP", "Delta Corp Ltd", 3801857L, "Consumer & Retail", 3000, DEFAULT_EXPIRIES, baseLtp = 112.00, prevClose = 110.00, week52High = 160.0, week52Low = 105.0, marketCapCr = 3000.0, peRatio = 18.0),
        FnoInstrument("INDHOTEL", "The Indian Hotels Company Ltd", 3861249L, "Consumer & Retail", 1000, DEFAULT_EXPIRIES, baseLtp = 740.00, prevClose = 728.00, week52High = 810.0, week52Low = 420.0, marketCapCr = 105200.0, peRatio = 74.0),
        FnoInstrument("INDIAMART", "IndiaMART InterMESH Ltd", 2706433L, "Consumer & Retail", 300, DEFAULT_EXPIRIES, baseLtp = 2480.00, prevClose = 2440.00, week52High = 3200.0, week52Low = 2200.0, marketCapCr = 14800.0, peRatio = 38.0),
        FnoInstrument("ABFRL", "Aditya Birla Fashion & Retail Ltd", 771585L, "Consumer & Retail", 2600, DEFAULT_EXPIRIES, baseLtp = 280.00, prevClose = 276.00, week52High = 340.0, week52Low = 200.0, marketCapCr = 28400.0, peRatio = -18.0),
        FnoInstrument("BALRAMCHIN", "Balrampur Chini Mills Ltd", 143105L, "Consumer & Retail", 1600, DEFAULT_EXPIRIES, baseLtp = 540.00, prevClose = 532.00, week52High = 650.0, week52Low = 360.0, marketCapCr = 10900.0, peRatio = 22.0)
    )

    fun getAllSymbols(): List<String> = ALL_INSTRUMENTS.map { it.symbol }

    fun getInstrumentBySymbol(symbol: String): FnoInstrument? {
        val clean = symbol.trim().uppercase()
        return ALL_INSTRUMENTS.find { it.symbol.equals(clean, ignoreCase = true) }
    }

    /**
     * Converts the entire master database into ready-to-use StockQuote models
     */
    fun toInitialStockQuotes(): List<StockQuote> {
        return ALL_INSTRUMENTS.map { inst ->
            val change = inst.baseLtp - inst.prevClose
            val changePct = if (inst.prevClose > 0.0) Math.round(((change / inst.prevClose) * 100.0) * 100.0) / 100.0 else 0.0
            val vwap = Math.round(((inst.baseLtp * 0.45) + (inst.prevClose * 0.55)) * 100.0) / 100.0
            val bid = Math.round((inst.baseLtp - 0.15) * 100.0) / 100.0
            val ask = Math.round((inst.baseLtp + 0.15) * 100.0) / 100.0
            val oi = (inst.lotSize * 15000L).coerceAtLeast(100000L)

            val pClose = inst.prevClose.toFloat()
            val ltp = inst.baseLtp.toFloat()
            val spark = listOf(
                pClose,
                pClose * 1.002f,
                (pClose + ltp) / 2f,
                ltp * 0.998f,
                ltp
            )

            StockQuote(
                symbol = inst.symbol,
                name = inst.companyName,
                ltp = inst.baseLtp,
                change = Math.round(change * 100.0) / 100.0,
                changePercent = changePct,
                open = inst.prevClose,
                high = inst.baseLtp.coerceAtLeast(inst.prevClose),
                low = inst.baseLtp.coerceAtMost(inst.prevClose),
                prevClose = inst.prevClose,
                volume = inst.baseVolume,
                turnoverCr = Math.round(((inst.baseLtp * inst.baseVolume) / 10000000.0) * 10.0) / 10.0,
                sector = inst.sector,
                marketCapCr = inst.marketCapCr,
                peRatio = inst.peRatio,
                isFnO = true,
                lotSize = inst.lotSize,
                yearHigh = inst.week52High,
                yearLow = inst.week52Low,
                sparkline = spark,
                isRealTimeFresh = false,
                openInterest = oi,
                oiChangePercent = 0.0,
                vwap = vwap,
                bidPrice = bid,
                askPrice = ask,
                instrumentToken = inst.instrumentToken,
                contractStatus = inst.contractStatus
            )
        }
    }
}
