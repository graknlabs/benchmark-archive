export const getOutliers = (numbers) => {
  const medianIndex = getMedian(numbers).index;
  const lowerHalf = numbers.slice(0, medianIndex);
  const q1 = getMedian(lowerHalf).value;
  const upperHalf = numbers.slice(medianIndex);
  const q3 = getMedian(upperHalf).value;
  const qr = q3 - q1;
  const upperBound = q3 + (1.5 * qr);
  const lowerBound = q1 - (1.5 * qr);

  return {
    upper: numbers.filter(item => item > upperBound),
    lower: numbers.filter(item => item < lowerBound),
  };
};

export const getMedian = (numbers) => {
  numbers.sort((a, b) => (a > b ? 1 : -1));
  const lowMiddleIndex = Math.floor((numbers.length - 1) / 2);
  const highMiddleIndex = Math.ceil((numbers.length - 1) / 2);
  return {
    value: (numbers[lowMiddleIndex] + numbers[highMiddleIndex]) / 2,
    index: lowMiddleIndex,
  };
};

export const getMean = numbers => getSum(numbers) / numbers.length;

export const getStdDeviation = (numbers) => {
  const mean = getMean(numbers);
  const sum = getSum(numbers.map(number => (number - mean) ** 2));
  return Math.sqrt(sum / numbers.length);
};

const getSum = numbers => numbers.reduce((a, b) => a + b, 0);
